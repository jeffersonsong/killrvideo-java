package com.killrvideo.service.video.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.killrvideo.dse.dto.CustomPagingState;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.video.dao.*;
import com.killrvideo.service.video.dto.LatestVideo;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.dto.UserVideo;
import com.killrvideo.service.video.dto.Video;
import com.killrvideo.utils.MappedAsyncPagingIterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static com.killrvideo.dse.dto.AbstractVideo.COLUMN_NAME;
import static com.killrvideo.dse.dto.AbstractVideo.COLUMN_PREVIEW;
import static com.killrvideo.dse.dto.Video.*;
import static com.killrvideo.service.video.dto.LatestVideo.COLUMN_YYYYMMDD;
import static com.killrvideo.utils.PageableQueryUtils.buildStatement;
import static com.killrvideo.utils.PageableQueryUtils.queryAsyncWithPagination;

/**
 * Implementations of operation for Videos.
 *
 * @author Jefferson Song
 */
@Repository
public class VideoCatalogRepository {
    /**
     * Loger for that class.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(VideoCatalogRepository.class);
    private static final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private CqlSession session;
    private VideoDao videoDao;
    private LatestVideoDao latestVideoDao;
    private UserVideoDao userVideoDao;

    /**
     * Prepare Statements 'getLatestVideso'.
     */
    private PreparedStatement latestVideoPreview_startingPointPrepared;
    private PreparedStatement latestVideoPreview_noStartingPointPrepared;

    /**
     * Prepare Statements 'getUserVideo'.
     */
    protected PreparedStatement userVideoPreview_startingPointPrepared;
    protected PreparedStatement userVideoPreview_noStartingPointPrepared;

    public VideoCatalogRepository(CqlSession session) {
        this.session = session;
        VideoCatalogMapper mapper = VideoCatalogMapper.build(session).build();
        this.videoDao = mapper.getVideoDao();
        this.userVideoDao = mapper.getUserVideoDao();
        this.latestVideoDao = mapper.getLatestVideoDao();

        this.latestVideoPreview_startingPointPrepared = session.prepare(
                "SELECT * " +
                        "FROM killrvideo.latest_videos " +
                        "WHERE yyyymmdd = ? " +
                        "AND (added_date, videoid) <= (:ad, :vid)");
        this.latestVideoPreview_noStartingPointPrepared = session.prepare(
                "SELECT * " +
                        "FROM killrvideo.latest_videos " +
                        "WHERE yyyymmdd = :ymd ");

        this.userVideoPreview_startingPointPrepared = session.prepare(
                "SELECT * " +
                        "FROM killrvideo.user_videos " +
                        "WHERE userid = :uid " +
                        "AND (added_date, videoid) <= (:ad, :vid)");
        this.userVideoPreview_noStartingPointPrepared = session.prepare(
                "SELECT * " +
                        "FROM killrvideo.user_videos " +
                        "WHERE userid = :uid ");
    }

    /**
     * Insert a VIDEO in the DB (ASYNC).
     */
    public CompletableFuture<Void> insertVideoAsync(Video v) {
        return this.videoDao.insert(v);
    }

    public CompletableFuture<Video> getVideoById(UUID videoid) {
        return this.videoDao.getVideoById(videoid);
    }

    public CompletableFuture<List<Video>> getVideoPreview(List<UUID> listofVideoId) {
        Assert.notNull(listofVideoId, "videoid list cannot be null");

        return this.videoDao.getVideoPreview(listofVideoId)
                .thenApply(MappedAsyncPagingIterableUtils::toList);
    }

    /**
     * Read a page of video preview for a user.
     *
     * @param userId            user unique identifier
     * @param startingVideoId   starting video if paging
     * @param startingAddedDate added date if paging
     * @param pagingState       paging state if paging
     * @return requested video (page)
     */
    public CompletableFuture<ResultListPage<UserVideo>> getUserVideosPreview(UUID userId,
                                                                             Optional<UUID> startingVideoId,
                                                                             Optional<Instant> startingAddedDate,
                                                                             Optional<Integer> pageSize,
                                                                             Optional<String> pagingState) {
        BoundStatement boundStatement = buildUserVideosPreviewStatement(userId, startingVideoId,
                startingAddedDate, pageSize, pagingState);

        return queryAsyncWithPagination(session, boundStatement, this::mapToUserVideo);
    }

    private BoundStatement buildUserVideosPreviewStatement(
            UUID userId,
            Optional<UUID> startingVideoId,
            Optional<Instant> startingAddedDate,
            Optional<Integer> pageSize,
            Optional<String> pagingState
    ) {
        if (startingVideoId.isPresent() && startingAddedDate.isPresent()) {
            return buildStatement(
                    userVideoPreview_startingPointPrepared,
                    stmt -> stmt.boundStatementBuilder(
                            userId,
                            startingAddedDate.get(),
                            startingVideoId.get()
                    ),
                    pageSize,
                    pagingState,
                    ConsistencyLevel.LOCAL_QUORUM);
        } else {
            return buildStatement(
                    userVideoPreview_noStartingPointPrepared,
                    stmt -> stmt.boundStatementBuilder(
                            userId
                    ),
                    pageSize,
                    pagingState,
                    ConsistencyLevel.LOCAL_QUORUM);
        }
    }

    /**
     * Build the first paging state if one does not already exist and return an object containing 3 elements
     * representing the initial state (List<String>, Integer, String).
     * @return CustomPagingState
     */
    public CustomPagingState buildFirstCustomPagingState() {
        return new CustomPagingState()
                .currentBucket(0)
                .cassandraPagingState(null)
                .listOfBuckets(LongStream.rangeClosed(0L, 7L).boxed()
                        .map(Instant.now().atZone(ZoneId.systemDefault())::minusDays)
                        .map(x -> x.format(DATEFORMATTER))
                        .collect(Collectors.toList()));
    }

    /**
     * Latest video partition key is the Date. As such we need to perform a query per date. As the user
     * ask for a number of video on a given page we may have to trigger several queries, on for each day.
     * To do it we implement a couple
     * <p>
     * For those of you wondering where the call to fetchMoreResults() is take a look here for an explanation.
     * https://docs.datastax.com/en/drivers/java/3.2/com/datastax/driver/core/PagingIterable.html#getAvailableWithoutFetching--
     * <p>
     * Quick summary, when getAvailableWithoutFetching() == 0 it automatically calls fetchMoreResults()
     * We could use it to force a fetch in a "prefetch" scenario, but that is not what we are doing here.
     *
     * @throws ExecutionException   error duing invoation
     * @throws InterruptedException error in asynchronism
     */
    public LatestVideosPage getLatestVideoPreviews(CustomPagingState cpState, int pageSize, Optional<Instant> startDate, Optional<UUID> startVid)
            throws InterruptedException, ExecutionException {
        LatestVideosPage returnedPage = new LatestVideosPage();
        LOGGER.debug("Looking for {} latest video(s)", pageSize);

        // Flag to synchronize usage of cassandra paging state
        final AtomicBoolean isCassandraPageState = new AtomicBoolean(false);

        do {

            // (1) - Paging state (custom or cassandra)
            final Optional<String> pagingState =
                    Optional.ofNullable(cpState.getCassandraPagingState())  // Only if present .get()
                            .filter(StringUtils::isNotBlank)                // ..and not empty
                            .filter(pg -> !isCassandraPageState.get());     // ..and cassandra paging is off

            // (2) - Build Query for a single bucket (=a single date)
            BoundStatement stmt = buildStatementLatestVideoPage(
                    cpState.getCurrentBucketValue(),          // Current Bucket Date yyyymmdd
                    pagingState,                              // Custom or cassandra pageing state
                    isCassandraPageState,                     // Flag to use and update cassandra paging
                    startDate, startVid,                      // Optional Parameters for filtering
                    pageSize - returnedPage.getResultSize()); // Number of element to retrieve from current query

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" + Executing {} with :ymd='{}' fetchSize={} and pagingState={}",
                        stmt.getPreparedStatement().getQuery(),
                        cpState.getCurrentBucketValue(),
                        stmt.getPageSize(),
                        pagingState.isPresent()
                );
            }

            // (3) - Execute Query Asynchronously
            CompletionStage<LatestVideosPage> cfv =
                    this.session.executeAsync(stmt).thenApply(this::mapLatestVideosResultAsPage);

            // (4) - Wait for result before triggering auery for page N+1
            LatestVideosPage currentPage = cfv.toCompletableFuture().get();
            returnedPage.getListOfPreview().addAll(currentPage.getListOfPreview());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" + bucket:{}/{} with results:{}/{} and pagingState:{}",cpState.getCurrentBucket(),
                        cpState.getListOfBucketsSize(), returnedPage.getResultSize(), pageSize, returnedPage.getCassandraPagingState());
            }

            // (5) Update NEXT PAGE BASE on current status
            if (returnedPage.getResultSize() == pageSize) {
                if (!StringUtils.isBlank(currentPage.getCassandraPagingState())) {
                    returnedPage.setNextPageState(createPagingState(cpState.getListOfBuckets(),
                            cpState.getCurrentBucket(), currentPage.getCassandraPagingState()));
                    LOGGER.debug(" + Exiting because we got enought results.");
                }
                // --> Start from the beginning of the next bucket since we're out of rows in this one
            } else if (cpState.getCurrentBucket() == cpState.getListOfBucketsSize() - 1) {
                returnedPage.setNextPageState(createPagingState(cpState.getListOfBuckets(), cpState.getCurrentBucket() + 1, ""));
                LOGGER.debug(" + Exiting because we are out of Buckets even if not enough results");
            }

            // (6) Move to next BUCKET
            cpState.incCurrentBucketIndex();

        } while ((returnedPage.getListOfPreview().size() < pageSize)               // Result has enough element to fill the page
                && cpState.getCurrentBucket() < cpState.getListOfBucketsSize()); // No nore bucket available

        return returnedPage;
    }

    private BoundStatement buildStatementLatestVideoPage(
            String yyyymmdd, Optional<String> pagingState, AtomicBoolean cassandraPagingStateUsed,
            Optional<Instant> startingAddedDate, Optional<UUID> startingVideoId, int recordNeeded) {

        BoundStatementBuilder statementBuilder;
        if (startingAddedDate.isPresent() && startingVideoId.isPresent()) {
            statementBuilder = latestVideoPreview_startingPointPrepared.boundStatementBuilder(
                    yyyymmdd, startingAddedDate.get(), startingVideoId.get()
            );
        } else {
            statementBuilder = latestVideoPreview_noStartingPointPrepared.boundStatementBuilder(yyyymmdd);
        }
        statementBuilder.setPageSize(recordNeeded);
        // Use custom paging state if provided and no cassandra triggered
        pagingState.ifPresent(x -> {
            statementBuilder.setPagingState(Bytes.fromHexString(x));
            cassandraPagingStateUsed.compareAndSet(false, true);
        });
        statementBuilder.setConsistencyLevel(ConsistencyLevel.ONE);
        return statementBuilder.build();
    }

    /**
     * Create a paging state string from the passed in parameters
     * @param buckets
     * @param bucketIndex
     * @param rowsPagingState
     * @return String
     */
    private String createPagingState(List<String> buckets, int bucketIndex, String rowsPagingState) {
        StringJoiner joiner = new StringJoiner("_");
        buckets.forEach(joiner::add);
        return joiner.toString() + "," + bucketIndex + "," + rowsPagingState;
    }

    /**
     * Mapping for Cassandra Result to expected bean.
     *
     * @param rs
     *      current result set
     * @return
     *      expected bean
     */
    private LatestVideosPage mapLatestVideosResultAsPage(AsyncResultSet rs) {
        LatestVideosPage resultPage = new LatestVideosPage();
        if (rs.getExecutionInfo().getPagingState() != null) {
            resultPage.setCassandraPagingState(Bytes.toHexString(rs.getExecutionInfo().getPagingState()));
        }
        List<LatestVideo> listOfVideos = StreamSupport.stream(rs.currentPage().spliterator(), false)
                .map(this::mapToLatestVideo)
                .collect(Collectors.toList());
        resultPage.setListOfPreview(listOfVideos);
        return resultPage;
    }

    private UserVideo mapToUserVideo(Row row) {
        return new UserVideo(
                row.getUuid(COLUMN_USERID),
                row.getUuid(COLUMN_VIDEOID),
                row.getString(COLUMN_NAME),
                row.getString(COLUMN_PREVIEW),
                row.getInstant(COLUMN_ADDED_DATE)
        );
    }

    private LatestVideo mapToLatestVideo(Row row) {
        return new LatestVideo(
                row.getString(COLUMN_YYYYMMDD),
                row.getUuid(COLUMN_USERID),
                row.getUuid(COLUMN_VIDEOID),
                row.getString(COLUMN_NAME),
                row.getString(COLUMN_PREVIEW),
                row.getInstant(COLUMN_ADDED_DATE)
        );
    }
}
