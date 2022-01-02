package com.killrvideo.service.video.repository

import com.killrvideo.dse.dto.CustomPagingState
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.service.video.dao.LatestVideoRowMapper
import com.killrvideo.service.video.dto.LatestVideo
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData
import com.killrvideo.utils.test.CassandraTestUtilsKt.mockPageableQueryFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture

internal class LatestVideoPreviewsRepositoryTest {
    private lateinit var repository: LatestVideoPreviewsRepository
    private lateinit var findLatestVideoPreview_startingPoint: PageableQuery<LatestVideo>
    private lateinit var findLatestVideoPreview_noStartingPoint: PageableQuery<LatestVideo>

    @BeforeEach
    fun setUp() {
        findLatestVideoPreview_startingPoint = mockk()
        findLatestVideoPreview_noStartingPoint = mockk()
        val pageableQueryFactory = mockPageableQueryFactory(
            findLatestVideoPreview_startingPoint,
            findLatestVideoPreview_noStartingPoint
        )

        val latestVideoRowMapper = mockk<LatestVideoRowMapper>()
        repository = LatestVideoPreviewsRepository(pageableQueryFactory, latestVideoRowMapper)
    }


    private fun resultListPage(latestVideo: LatestVideo, pagingState: String): ResultListPage<LatestVideo> =
        ResultListPage(
            listOf(latestVideo),
            Optional.of(pagingState)
        )

    @Test
    fun testGetLatestVideoPreviewsAsyncWithoutStartingPoint() {
        val cpState = CustomPagingState.buildFirstCustomPagingState()
        val pageSize = 2
        val request = GetLatestVideoPreviewsRequestData(
            pageState = cpState,
            pageSize = pageSize,
            startDate = null,
            startVideoId = null
        )
        val latestVideo1 = mockk<LatestVideo>()
        val resultListPage1 = resultListPage(
            latestVideo1, "paging state 1"
        )
        val latestVideo2 = mockk<LatestVideo>()
        val resultListPage2 = resultListPage(
            latestVideo2, "paging state 2"
        )
        every {
            findLatestVideoPreview_noStartingPoint.queryNext(any(), any(), any())
        } returnsMany
                listOf(
                    CompletableFuture.completedFuture(resultListPage1),
                    CompletableFuture.completedFuture(resultListPage2)
                )

        val result = runBlocking { repository.getLatestVideoPreviewsAsync(request) }
        assertThat(result.listOfPreview, hasItems(latestVideo1, latestVideo2))
    }

    @Test
    fun testGetLatestVideoPreviewsAsyncWithStartingPoint() {
        val cpState = CustomPagingState.buildFirstCustomPagingState()
        val pageSize = 2
        val startingVideoid = UUID.randomUUID()
        val startingDate = Instant.now().minus(5, ChronoUnit.DAYS)
        val request = GetLatestVideoPreviewsRequestData(
            pageState = cpState,
            pageSize = pageSize,
            startDate = startingDate,
            startVideoId = startingVideoid
        )
        val latestVideo1 = mockk<LatestVideo>()
        val resultListPage1 = resultListPage(
            latestVideo1, "paging state 1"
        )
        val latestVideo2 = mockk<LatestVideo>()
        val resultListPage2 = resultListPage(
            latestVideo2, "paging state 2"
        )
        every {
            findLatestVideoPreview_startingPoint.queryNext(any(), any(), *anyVararg())
        } returnsMany
                listOf(
                    CompletableFuture.completedFuture(resultListPage1),
                    CompletableFuture.completedFuture(resultListPage2)
                )

        val result = runBlocking { repository.getLatestVideoPreviewsAsync(request) }
        assertThat(result.listOfPreview, hasItems(latestVideo1, latestVideo2))
    }
}
