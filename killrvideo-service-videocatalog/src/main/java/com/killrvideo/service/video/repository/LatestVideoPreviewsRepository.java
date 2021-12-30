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
                                         VideoCatalogMapper mapper,
                                         LatestVideoRowMapper latestVideoRowMapper) {
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
            final CustomPagingState cpState,
            final int pageSize,
            final Optional<Instant> startDate,
            final Optional<UUID> startVid
    ) throws InterruptedException, ExecutionException {
        LatestVideosPage returnedPage = new LatestVideosPage();
        LOGGER.debug("Looking for {} latest video(s)", pageSize);

        CustomPagingState currState = cpState;

        do {
            // (1) - Paging state (custom or cassandra)
            final Optional<String> pagingState =
                    Optional.ofNullable(currState.getCassandraPagingState())  // Only if present .get()
                            .filter(StringUtils::isNotBlank);                // ..and not empty

            GetLatestVideoPreviewsForGivenDateRequestData query =
                    new GetLatestVideoPreviewsForGivenDateRequestData(
                            currState.getCurrentBucketValue(),
                            pagingState,
                            pageSize - returnedPage.getResultSize(),
                            startDate,
                            startVid
                    );

            ResultListPage<LatestVideo> currentPage =
                    loadCurrentPage(query).get();

            returnedPage.getListOfPreview().addAll(currentPage.getResults());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" + bucket:{}/{} with results:{}/{} and pagingState:{}",
                        currState.getCurrentBucket(),
                        currState.getListOfBucketsSize(),
                        returnedPage.getResultSize(),
                        pageSize,
                        returnedPage.getCassandraPagingState()
                );
            }

            currState = nextState(
                    currState,
                    currentPage.getPagingState(),
                    gotEnough(returnedPage, pageSize)
            );

        } while ((returnedPage.getListOfPreview().size() < pageSize)               // Result has enough element to fill the page
                && currState.getCurrentBucket() < currState.getListOfBucketsSize()); // No nore bucket available

        returnedPage.setNextPageState(currState.serialize());

        return returnedPage;
    }

    /**
     * Load current page.
     *
     * @param request Query for latest videos for given date.
     * @return latest videos for current bucket date.
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

    private boolean gotEnough(LatestVideosPage returnedPage, int pageSize) {
        return returnedPage.getResultSize() == pageSize;
    }

    private CustomPagingState nextState(
            CustomPagingState cpState,
            Optional<String> pagingState,
            boolean gotEnough) {
        return gotEnough ?
                cpState.changeCassandraPagingState(pagingState.orElse("")):
                cpState.incCurrentBucketIndex();
    }
}
