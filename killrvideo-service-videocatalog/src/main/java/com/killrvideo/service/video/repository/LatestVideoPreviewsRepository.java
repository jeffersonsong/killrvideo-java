package com.killrvideo.service.video.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.killrvideo.dse.dto.CustomPagingState;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;
import com.killrvideo.service.video.dao.LatestVideoDao;
import com.killrvideo.service.video.dao.LatestVideoRowMapper;
import com.killrvideo.service.video.dao.VideoCatalogMapper;
import com.killrvideo.service.video.dto.LatestVideo;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsForGivenDateRequestData;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final LatestVideoDao latestVideoDao;
    /**
     * Prepare Statements 'getLatestVideso'.
     */
    private final PageableQuery<LatestVideo> findLatestVideoPreview_startingPoint;
    private final PageableQuery<LatestVideo> findLatestVideoPreview_noStartingPoint;

    public LatestVideoPreviewsRepository(PageableQueryFactory pageableQueryFactory,
                                         VideoCatalogMapper mapper, LatestVideoRowMapper latestVideoRowMapper) {
        this.latestVideoDao = mapper.getLatestVideoDao();

        this.findLatestVideoPreview_startingPoint = pageableQueryFactory.newPageableQuery(
                QUERY_LATEST_VIDEO_PREVIEW_STARTING_POINT,
                ConsistencyLevel.LOCAL_ONE,
                latestVideoRowMapper::map
        );
        this.findLatestVideoPreview_noStartingPoint = pageableQueryFactory.newPageableQuery(
                QUERY_LATEST_VIDEO_PREVIEW_NO_STARTING_POINT,
                ConsistencyLevel.LOCAL_ONE,
                latestVideoRowMapper::map
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
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private LatestVideosPage getLatestVideoPreviews(
            CustomPagingState cpState,
            int pageSize,
            Optional<Instant> startDate,
            Optional<UUID> startVid
    ) throws InterruptedException, ExecutionException {
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

            GetLatestVideoPreviewsForGivenDateRequestData query =
                    new GetLatestVideoPreviewsForGivenDateRequestData(
                            cpState.getCurrentBucketValue(),
                            pagingState,
                            pageSize - returnedPage.getResultSize(),
                            startDate,
                            startVid
                    );
            ResultListPage<LatestVideo> currentPage = loadCurrentPage(query).get();

            pagingState.ifPresent(x -> isCassandraPageState.compareAndSet(false, true));

            returnedPage.getListOfPreview().addAll(currentPage.getResults());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" + bucket:{}/{} with results:{}/{} and pagingState:{}",
                        cpState.getCurrentBucket(),
                        cpState.getListOfBucketsSize(),
                        returnedPage.getResultSize(),
                        pageSize,
                        returnedPage.getCassandraPagingState()
                );
            }

            // (5) Update NEXT PAGE BASE on current status
            updateNextPage(returnedPage, cpState, pageSize, currentPage.getPagingState());

            // (6) Move to next BUCKET
            cpState = cpState.incCurrentBucketIndex();

        } while ((returnedPage.getListOfPreview().size() < pageSize)               // Result has enough element to fill the page
                && cpState.getCurrentBucket() < cpState.getListOfBucketsSize()); // No nore bucket available

        return returnedPage;
    }

    /**
     * Load current page.
     *
     * @param request Query for latest videos for given date.
     * @return latest videos for current bucket date.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private CompletableFuture<ResultListPage<LatestVideo>> loadCurrentPage(
            GetLatestVideoPreviewsForGivenDateRequestData request
    ) {
        if (request.getStartDate().isPresent() && request.getStartVideoId().isPresent()) {
            return findLatestVideoPreview_startingPoint.queryNext(
                    Optional.of(request.getPageSize()),
                    request.getPagingState(),
                    request.getYyyymmdd(),
                    request.getStartDate().get(),
                    request.getStartVideoId().get()
            );
        } else {
            return findLatestVideoPreview_noStartingPoint.queryNext(
                    Optional.of(request.getPageSize()),
                    request.getPagingState(),
                    request.getYyyymmdd()
            );
        }
    }

    /**
     * Update NEXT PAGE BASE on current status
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void updateNextPage(LatestVideosPage returnedPage,
                                CustomPagingState cpState,
                                int pageSize,
                                Optional<String> currentCassandraPagingState) {
        if (returnedPage.getResultSize() == pageSize) {
            if (isNotBlank(currentCassandraPagingState)) {
                //noinspection OptionalGetWithoutIsPresent
                returnedPage.setNextPageState(
                        cpState.changeCassandraPagingState(currentCassandraPagingState.get())
                                .serialize()
                );
                LOGGER.debug(" + Exiting because we got enough results.");
            }

        } else if (cpState.getCurrentBucket() == cpState.getListOfBucketsSize() - 1) {
            // --> Start from the beginning of the next bucket since we're out of rows in this one
            returnedPage.setNextPageState(
                    cpState.incCurrentBucketIndex().changeCassandraPagingState("").serialize()
            );
            LOGGER.debug(" + Exiting because we are out of Buckets even if not enough results");
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private boolean isNotBlank(Optional<String> pagingState) {
        return pagingState.isPresent() && StringUtils.isNotBlank(pagingState.get());
    }
}
