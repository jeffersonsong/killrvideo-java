package com.killrvideo.service.video.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.killrvideo.dse.dto.CustomPagingState;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;
import com.killrvideo.service.video.dao.*;
import com.killrvideo.service.video.dto.*;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.dse.utils.MappedAsyncPagingIterableUtils;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static com.killrvideo.dse.dto.CustomPagingState.createPagingState;

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
public class VideoCatalogRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoCatalogRepository.class);

    private static final String QUERY_LATEST_VIDEO_PREVIEW_STARTING_POINT =
            "SELECT * " +
            "FROM killrvideo.latest_videos " +
            "WHERE yyyymmdd = ? " +
            "AND (added_date, videoid) <= (:ad, :vid)";
    private static final String QUERY_LATEST_VIDEO_PREVIEW_NO_STARTING_POINT =
            "SELECT * " +
            "FROM killrvideo.latest_videos " +
            "WHERE yyyymmdd = :ymd ";

    private static final String QUERY_USER_VIDEO_PREVIEW_STARTING_POINT =
            "SELECT * " +
            "FROM killrvideo.user_videos " +
            "WHERE userid = :uid " +
            "AND (added_date, videoid) <= (:ad, :vid)";
    private static final String QUERY_USER_VIDEO_PREVIEW_NO_STARTING_POINT =
            "SELECT * " +
            "FROM killrvideo.user_videos " +
            "WHERE userid = :uid ";


    private final CqlSession session;
    private final VideoDao videoDao;
    private final UserVideoDao userVideoDao;
    private final LatestVideoDao latestVideoDao;
    private final LatestVideoRowMapper latestVideoRowMapper;

    /**
     * Prepare Statements 'getLatestVideso'.
     */
    private final PreparedStatement latestVideoPreview_startingPointPrepared;
    private final PreparedStatement latestVideoPreview_noStartingPointPrepared;

    /**
     * Prepare Statements 'getUserVideo'.
     */
    protected final PageableQuery<UserVideo> findUserVideoPreview_startingPoint;
    protected final PageableQuery<UserVideo> findUserVideoPreview_noStartingPoint;

    public VideoCatalogRepository(CqlSession session,
                                  PageableQueryFactory pageableQueryFactory,
                                  VideoCatalogMapper mapper,
                                  UserVideoRowMapper userVideoRowMapper,
                                  LatestVideoRowMapper latestVideoRowMapper) {
        this.session = session;
        this.latestVideoRowMapper = latestVideoRowMapper;
        this.videoDao = mapper.getVideoDao();
        this.userVideoDao = mapper.getUserVideoDao();
        this.latestVideoDao = mapper.getLatestVideoDao();

        this.latestVideoPreview_startingPointPrepared = session.prepare(
                QUERY_LATEST_VIDEO_PREVIEW_STARTING_POINT
        );
        this.latestVideoPreview_noStartingPointPrepared = session.prepare(
                QUERY_LATEST_VIDEO_PREVIEW_NO_STARTING_POINT
        );

        this.findUserVideoPreview_startingPoint = pageableQueryFactory.newPageableQuery(
                QUERY_USER_VIDEO_PREVIEW_STARTING_POINT,
                ConsistencyLevel.LOCAL_QUORUM,
                userVideoRowMapper::map
        );
        this.findUserVideoPreview_noStartingPoint = pageableQueryFactory.newPageableQuery(
                QUERY_USER_VIDEO_PREVIEW_NO_STARTING_POINT,
                ConsistencyLevel.LOCAL_QUORUM,
                userVideoRowMapper::map
        );
    }

    /**
     * Insert a VIDEO in the DB (ASYNC).
     */
    public CompletableFuture<Void> insertVideoAsync(Video v) {
        Instant now = Instant.now();
        v.setAddedDate(now);

        return CompletableFuture.allOf(
                this.videoDao.insert(v),
                this.userVideoDao.insert(UserVideo.from(v, now)),
                this.latestVideoDao.insert(LatestVideo.from(v, now))
        );
    }

    public CompletableFuture<Video> getVideoById(UUID videoid) {
        return this.videoDao.getVideoById(videoid);
    }

    public CompletableFuture<List<Video>> getVideoPreview(List<UUID> listofVideoId) {
        Assert.notNull(listofVideoId, "videoid list cannot be null");

        return this.videoDao.getVideoPreview(listofVideoId)
                .thenApply(MappedAsyncPagingIterableUtils::all);
    }

    /**
     * Read a page of video preview for a user.
     *
     * @param request request.
     * @return requested video (page)
     */
    public CompletableFuture<ResultListPage<UserVideo>> getUserVideosPreview(GetUserVideoPreviewsRequestData request) {
        if (request.getStartingVideoId().isPresent() && request.getStartingAddedDate().isPresent()) {
            return getUserVideosPreviewWithStartingPoint(request);
        } else {
            return getUserVideosPreviewWithoutStartingPoint(request);
        }
    }

    private CompletableFuture<ResultListPage<UserVideo>> getUserVideosPreviewWithStartingPoint(
            GetUserVideoPreviewsRequestData request
    ) {
        return findUserVideoPreview_startingPoint.queryNext(
                request.getPagingSize(),
                request.getPagingState(),
                request.getUserId(),
                request.getStartingAddedDate().get(),
                request.getStartingVideoId().get()
        );
    }

    private CompletableFuture<ResultListPage<UserVideo>> getUserVideosPreviewWithoutStartingPoint(
            GetUserVideoPreviewsRequestData request
    ) {
        return findUserVideoPreview_noStartingPoint.queryNext(
                request.getPagingSize(),
                request.getPagingState(),
                request.getUserId()
        );
    }

    public CompletableFuture<LatestVideosPage> getLatestVideoPreviewsAsync(
            GetLatestVideoPreviewsRequestData request
    ) {
        try {
            LatestVideosPage returnedPage = getLatestVideoPreviews(
                    request.getPageState(),
                    request.getPageSize(),
                    request.getStartDate(),
                    request.getStartVideoId()
            );
            return CompletableFuture.completedFuture(returnedPage);
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        } finally {
            LOGGER.debug("End getting latest video preview");
        }
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
    private LatestVideosPage getLatestVideoPreviews(CustomPagingState cpState, int pageSize, Optional<Instant> startDate, Optional<UUID> startVid)
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
                LOGGER.debug(" + bucket:{}/{} with results:{}/{} and pagingState:{}", cpState.getCurrentBucket(),
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
     * Mapping for Cassandra Result to expected bean.
     *
     * @param rs current result set
     * @return expected bean
     */
    private LatestVideosPage mapLatestVideosResultAsPage(AsyncResultSet rs) {
        LatestVideosPage resultPage = new LatestVideosPage();
        if (rs.getExecutionInfo().getPagingState() != null) {
            resultPage.setCassandraPagingState(Bytes.toHexString(rs.getExecutionInfo().getPagingState()));
        }
        List<LatestVideo> listOfVideos = StreamSupport.stream(rs.currentPage().spliterator(), false)
                .map(this.latestVideoRowMapper::map)
                .collect(Collectors.toList());
        resultPage.setListOfPreview(listOfVideos);
        return resultPage;
    }
}
