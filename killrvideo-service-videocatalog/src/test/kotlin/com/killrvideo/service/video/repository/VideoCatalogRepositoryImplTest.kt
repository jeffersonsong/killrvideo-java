package com.killrvideo.service.video.repository

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.service.video.dao.*
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData
import com.killrvideo.utils.test.CassandraTestUtilsKt.mockMappedAsyncPagingIterable
import com.killrvideo.utils.test.CassandraTestUtilsKt.mockPageableQueryFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture

internal class VideoCatalogRepositoryImplTest {
    private lateinit var repository: VideoCatalogRepositoryImpl
    private lateinit var videoDao: VideoDao
    private lateinit var userVideoDao: UserVideoDao
    private lateinit var latestVideoDao: LatestVideoDao
    private lateinit var latestVideoPreviewsRequestRepository: LatestVideoPreviewsRepository
    private lateinit var findUserVideoPreview_startingPoint: PageableQuery<UserVideo?>
    private lateinit var findUserVideoPreview_noStartingPoint: PageableQuery<UserVideo?>

    @BeforeEach
    fun setUp() {
        findUserVideoPreview_startingPoint = mockk()
        findUserVideoPreview_noStartingPoint = mockk()
        val pageableQueryFactory = mockPageableQueryFactory(
            findUserVideoPreview_startingPoint,
            findUserVideoPreview_noStartingPoint
        )

        val mapper = mockk<VideoCatalogMapper>()
        videoDao = mockk()
        userVideoDao = mockk()
        latestVideoDao = mockk()
        every { mapper.videoDao } returns videoDao
        every { mapper.userVideoDao } returns userVideoDao
        every { mapper.latestVideoDao } returns latestVideoDao

        val userVideoRowMapper = mockk<UserVideoRowMapper>()
        latestVideoPreviewsRequestRepository = mockk()

        repository = VideoCatalogRepositoryImpl(
            pageableQueryFactory, mapper, userVideoRowMapper, latestVideoPreviewsRequestRepository
        )
    }

    @Test
    fun testInsertVideoAsync() {
        val v = video()
        every { videoDao.insert(any()) } returns CompletableFuture.completedFuture(null)
        every { userVideoDao.insert(any()) } returns CompletableFuture.completedFuture(null)
        every { latestVideoDao.insert(any()) } returns CompletableFuture.completedFuture(null)

        runBlocking { repository.insertVideoAsync(v) }
        verify(exactly = 1) {
            videoDao.insert(any())
            userVideoDao.insert(any())
            latestVideoDao.insert(any())
        }
    }

    @Test
    fun testGetVideoById() {
        val videoid = UUID.randomUUID()
        val video = mockk<Video>()
        every { videoDao.getVideoById(any()) } returns CompletableFuture.completedFuture(video)
        val result = runBlocking { repository.getVideoById(videoid) }
        assertNotNull(result)
        assertEquals(video, result)
    }

    @Test
    fun testGetVideoPreview() {
        val videoid = UUID.randomUUID()
        val video = mockk<Video>()
        val videoids = listOf(videoid)
        val videos = listOf(video)
        val iter: MappedAsyncPagingIterable<Video> = mockMappedAsyncPagingIterable(videos)
        every { videoDao.getVideoPreview(any()) } returns CompletableFuture.completedFuture(iter)
        val result = runBlocking { repository.getVideoPreview(videoids) }
        assertEquals(videos, result)
    }

    @Test
    fun testGetUserVideosPreviewWithoutStartingPoint() {
        val resultListPage: ResultListPage<UserVideo?> = mockk()
        every {
            findUserVideoPreview_noStartingPoint.queryNext(any(), any(), any())
        } returns CompletableFuture.completedFuture(resultListPage)

        val userid = UUID.randomUUID()
        val request = GetUserVideoPreviewsRequestData(userid)
        val result = runBlocking { repository.getUserVideosPreview(request) }
        assertEquals(resultListPage, result)
    }

    @Test
    fun testGetUserVideosPreviewWithStartingPoint() {
        val resultListPage: ResultListPage<UserVideo?> = mockk()
        every {
            findUserVideoPreview_startingPoint.queryNext(any(), any(), *anyVararg())
        } returns CompletableFuture.completedFuture(resultListPage)
        val userid = UUID.randomUUID()
        val videoid = UUID.randomUUID()
        val startingDate = Instant.now().minus(1, ChronoUnit.DAYS)
        val request = GetUserVideoPreviewsRequestData(
            userId = userid,
            startingVideoId = videoid,
            startingAddedDate = startingDate,
            pagingSize = 2,
            pagingState = null
        )
        val result = runBlocking { repository.getUserVideosPreview(request) }
        assertEquals(resultListPage, result)
    }

    @Test
    fun testGetLatestVideoPreviewsAsync() {
        val request = mockk<GetLatestVideoPreviewsRequestData>()
        val latestVideosPage = mockk<LatestVideosPage>()
        every {
            latestVideoPreviewsRequestRepository.getLatestVideoPreviewsAsync(request)
        } returns latestVideosPage
        val result = runBlocking { repository.getLatestVideoPreviewsAsync(request) }
        assertEquals(latestVideosPage, result)
    }

    private fun video() = Video(
        videoid = UUID.randomUUID(),
        userid = UUID.randomUUID(),
        name = "Game",
        description = "Game",
        location = "url",
        locationType = 1,
        previewImageLocation = "url",
        tags = mutableSetOf(),
        addedDate = null
    )
}
