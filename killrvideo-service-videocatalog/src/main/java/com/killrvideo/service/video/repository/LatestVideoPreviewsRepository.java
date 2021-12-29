package com.killrvideo.service.video.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.killrvideo.dse.dto.CustomPagingState;
import com.killrvideo.service.video.dao.LatestVideoDao;
import com.killrvideo.service.video.dao.LatestVideoRowMapper;
import com.killrvideo.service.video.dao.VideoCatalogMapper;
import com.killrvideo.service.video.dto.LatestVideo;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.killrvideo.dse.dto.CustomPagingState.createPagingState;
import static com.killrvideo.dse.utils.AsyncResultSetUtils.getPagingState;
import static com.killrvideo.dse.utils.AsyncResultSetUtils.getResultsOnCurrentPage;

@Component
public class LatestVideoPreviewsRepository {

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

    private final CqlSession session;
    private final LatestVideoDao latestVideoDao;
    private final LatestVideoRowMapper latestVideoRowMapper;
    /**
     * Prepare Statements 'getLatestVideso'.
     */
    private final PreparedStatement latestVideoPreview_startingPointPrepared;
    private final PreparedStatement latestVideoPreview_noStartingPointPrepared;

    public LatestVideoPreviewsRepository(CqlSession session, VideoCatalogMapper mapper, LatestVideoRowMapper latestVideoRowMapper) {
        this.session = session;
        this.latestVideoDao = mapper.getLatestVideoDao();
        this.latestVideoRowMapper = latestVideoRowMapper;

        this.latestVideoPreview_startingPointPrepared = session.prepare(
                QUERY_LATEST_VIDEO_PREVIEW_STARTING_POINT
        );
        this.latestVideoPreview_noStartingPointPrepared = session.prepare(
                QUERY_LATEST_VIDEO_PREVIEW_NO_STARTING_POINT
        );
    }

    public CompletableFuture<Void> insert(LatestVideo latestVideo) {
        return this.latestVideoDao.insert(latestVideo);
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
            CompletionStage<LatestVideosPage> cfv = this.session.executeAsync(stmt)
                    .thenApply(this::mapLatestVideosResultAsPage);

            // (4) - Wait for result before triggering query for page N+1
            LatestVideosPage currentPage = cfv.toCompletableFuture().get();

            returnedPage.getListOfPreview().addAll(currentPage.getListOfPreview());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" + bucket:{}/{} with results:{}/{} and pagingState:{}", cpState.getCurrentBucket(),
                        cpState.getListOfBucketsSize(), returnedPage.getResultSize(), pageSize, returnedPage.getCassandraPagingState());
            }

            // (5) Update NEXT PAGE BASE on current status
            if (returnedPage.getResultSize() == pageSize) {
                if (!StringUtils.isBlank(currentPage.getCassandraPagingState())) {
                    returnedPage.setNextPageState(
                            createPagingState(cpState.getListOfBuckets(), cpState.getCurrentBucket(),
                                    currentPage.getCassandraPagingState())
                    );
                    LOGGER.debug(" + Exiting because we got enought results.");
                }
                // --> Start from the beginning of the next bucket since we're out of rows in this one
            } else if (cpState.getCurrentBucket() == cpState.getListOfBucketsSize() - 1) {
                returnedPage.setNextPageState(
                        createPagingState(cpState.getListOfBuckets(), cpState.getCurrentBucket() + 1, "")
                );
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

        BoundStatementBuilder statementBuilder = getBoundStatementBuilder(yyyymmdd, startingAddedDate, startingVideoId);
        statementBuilder.setPageSize(recordNeeded);
        // Use custom paging state if provided and no cassandra triggered
        pagingState.ifPresent(x -> {
            statementBuilder.setPagingState(Bytes.fromHexString(x));
            cassandraPagingStateUsed.compareAndSet(false, true);
        });
        statementBuilder.setConsistencyLevel(ConsistencyLevel.ONE);
        return statementBuilder.build();
    }

    private BoundStatementBuilder getBoundStatementBuilder(String yyyymmdd, Optional<Instant> startingAddedDate, Optional<UUID> startingVideoId) {
        if (startingAddedDate.isPresent() && startingVideoId.isPresent()) {
            return latestVideoPreview_startingPointPrepared.boundStatementBuilder(
                    yyyymmdd, startingAddedDate.get(), startingVideoId.get()
            );
        } else {
            return latestVideoPreview_noStartingPointPrepared.boundStatementBuilder(yyyymmdd);
        }
    }

    /**
     * Mapping for Cassandra Result to expected bean.
     *
     * @param rs current result set
     * @return expected bean
     */
    private LatestVideosPage mapLatestVideosResultAsPage(AsyncResultSet rs) {
        LatestVideosPage resultPage = new LatestVideosPage();
        getPagingState(rs).ifPresent(resultPage::setCassandraPagingState);
        List<LatestVideo> listOfVideos = getResultsOnCurrentPage(rs, this.latestVideoRowMapper::map);
        resultPage.setListOfPreview(listOfVideos);
        return resultPage;
    }
}
