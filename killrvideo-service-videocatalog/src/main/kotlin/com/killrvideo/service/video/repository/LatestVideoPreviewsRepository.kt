package com.killrvideo.service.video.repository

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.dse.dto.CustomPagingState
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.dse.utils.PageableQueryFactory
import com.killrvideo.service.utils.ServiceGrpcUtils.toNullable
import com.killrvideo.service.video.dao.LatestVideoRowMapper
import com.killrvideo.service.video.dto.LatestVideo
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.repository.VideoCatalogRepository
import com.killrvideo.service.video.request.GetLatestVideoPreviewsForGivenDateRequestData
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData
import io.grpc.Status
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@Component
class LatestVideoPreviewsRepository(
    pageableQueryFactory: PageableQueryFactory,
    latestVideoRowMapper: LatestVideoRowMapper
) {
    private val findLatestVideoPreview_startingPoint: PageableQuery<LatestVideo>
    private val findLatestVideoPreview_noStartingPoint: PageableQuery<LatestVideo>

    init {
        findLatestVideoPreview_startingPoint = pageableQueryFactory.newPageableQuery(
            QUERY_LATEST_VIDEO_PREVIEW_STARTING_POINT,
            ConsistencyLevel.LOCAL_ONE
        ) { row: Row -> latestVideoRowMapper.map(row) }
        findLatestVideoPreview_noStartingPoint = pageableQueryFactory.newPageableQuery(
            QUERY_LATEST_VIDEO_PREVIEW_NO_STARTING_POINT,
            ConsistencyLevel.LOCAL_ONE
        ) { row: Row -> latestVideoRowMapper.map(row) }
    }

    fun getLatestVideoPreviewsAsync(
        request: GetLatestVideoPreviewsRequestData
    ): LatestVideosPage {
        try {
            return getLatestVideoPreviews(
                request.pageState,
                request.pageSize,
                request.startDate,
                request.startVideoId
            )
        } catch (ex: Exception) {
            throw Status.INTERNAL.withCause(ex).asRuntimeException()
        } finally {
            LOGGER.debug("End getting latest video preview")
        }
    }

    /**
     * Latest video partition key is the Date. As such we need to perform a query per date. As the user
     * ask for a number of video on a given page we may have to trigger several queries, on for each day.
     * To do it we implement a couple
     *
     *
     * For those of you wondering where the call to fetchMoreResults() is take a look here for an explanation.
     * https://docs.datastax.com/en/drivers/java/3.2/com/datastax/driver/core/PagingIterable.html#getAvailableWithoutFetching--
     *
     *
     * Quick summary, when getAvailableWithoutFetching() == 0 it automatically calls fetchMoreResults()
     * We could use it to force a fetch in a "prefetch" scenario, but that is not what we are doing here.
     *
     * @throws ExecutionException   error duing invoation
     * @throws InterruptedException error in asynchronism
     */
    @Throws(InterruptedException::class, ExecutionException::class)
    private fun getLatestVideoPreviews(
        cpState: CustomPagingState,
        pageSize: Int,
        startDate: Instant?,
        startVid: UUID?
    ): LatestVideosPage {
        val returnedPage = LatestVideosPage()
        LOGGER.debug("Looking for {} latest video(s)", pageSize)
        var currState = cpState
        do {
            // (1) - Paging state (custom or cassandra)
            val pagingState = Optional.ofNullable(currState.cassandraPagingState) // Only if present .get()
                .filter { cs: String? -> StringUtils.isNotBlank(cs) } // ..and not empty
            val query = GetLatestVideoPreviewsForGivenDateRequestData(
                currState.currentBucketValue,
                toNullable(pagingState),
                pageSize - returnedPage.resultSize,
                startDate,
                startVid
            )
            val currentPage = loadCurrentPage(query).get()

            currentPage.results.filter { Objects.nonNull(it) }.forEach { returnedPage.listOfPreview.add(it!!) }

            if (LOGGER.isDebugEnabled) {
                LOGGER.debug(
                    " + bucket:{}/{} with results:{}/{} and pagingState:{}",
                    currState.currentBucket,
                    currState.listOfBucketsSize,
                    returnedPage.resultSize,
                    pageSize,
                    returnedPage.cassandraPagingState
                )
            }
            currState = nextState(
                currState,
                currentPage.pagingState,
                gotEnough(returnedPage, pageSize)
            )
        } while (returnedPage.listOfPreview.size < pageSize // Result has enough element to fill the page
            && currState.currentBucket < currState.listOfBucketsSize
        ) // No nore bucket available
        returnedPage.nextPageState = currState.serialize()
        return returnedPage
    }


    /**
     * Load current page.
     *
     * @param request Query for latest videos for given date.
     * @return latest videos for current bucket date.
     */
    private fun loadCurrentPage(
        request: GetLatestVideoPreviewsForGivenDateRequestData
    ): CompletableFuture<ResultListPage<LatestVideo>> {
        return if (request.startDate != null && request.startVideoId != null) {
            findLatestVideoPreview_startingPoint.queryNext(
                request.pageSize,
                request.pagingState,
                request.yyyymmdd,
                request.startDate,
                request.startVideoId
            )
        } else {
            findLatestVideoPreview_noStartingPoint.queryNext(
                request.pageSize,
                request.pagingState,
                request.yyyymmdd
            )
        }
    }

    private fun gotEnough(returnedPage: LatestVideosPage, pageSize: Int): Boolean {
        return returnedPage.resultSize == pageSize
    }

    private fun nextState(
        cpState: CustomPagingState,
        pagingState: String?,
        gotEnough: Boolean
    ): CustomPagingState {
        return if (gotEnough)
            cpState.changeCassandraPagingState(pagingState ?: "")
        else
            cpState.incCurrentBucketIndex()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VideoCatalogRepository::class.java)
        private const val QUERY_LATEST_VIDEO_PREVIEW_STARTING_POINT = "SELECT * " +
                "FROM killrvideo.latest_videos " +
                "WHERE yyyymmdd = ? " +
                "AND (added_date, videoid) <= (:ad, :vid)"
        private const val QUERY_LATEST_VIDEO_PREVIEW_NO_STARTING_POINT = "SELECT * " +
                "FROM killrvideo.latest_videos " +
                "WHERE yyyymmdd = :ymd "
    }
}
