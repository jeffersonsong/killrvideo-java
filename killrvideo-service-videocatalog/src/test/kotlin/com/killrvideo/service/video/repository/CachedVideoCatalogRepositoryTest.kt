package com.killrvideo.service.video.repository

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class CachedVideoCatalogRepositoryTest {
    @InjectMockKs
    private lateinit var repository: CachedVideoCatalogRepository
    @MockK
    private lateinit var videoCatalogRepository: VideoCatalogRepository
    @MockK
    private lateinit var videoRedisRepository: VideoRedisRepository

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun testInsertVideoAsync() {
        coJustRun { videoCatalogRepository.insertVideoAsync(any()) }
        coJustRun { videoRedisRepository.insertVideoAsync(any()) }

        val video = mockk<Video>()
        runBlocking { repository.insertVideoAsync(video) }
        coVerify { videoCatalogRepository.insertVideoAsync(any()) }
        coVerify { videoRedisRepository.insertVideoAsync(any()) }
    }

    @Test
    fun testGetVideoByIdCacheHit() {
        val cached = mockk<Video>()
        coEvery { videoRedisRepository.getVideoById(any()) } returns cached

        val videoid = UUID.randomUUID()
        val result = runBlocking { repository.getVideoById(videoid) }
        assertEquals(cached, result)
    }

    @Test
    fun testGetVideoByIdCacheMiss() {
        coEvery { videoRedisRepository.getVideoById(any()) } returns null

        val v = mockk<Video>()
        coEvery { videoCatalogRepository.getVideoById(any()) } returns v
        coJustRun { videoRedisRepository.insertVideoAsync(any()) }

        val videoid = UUID.randomUUID()
        val result = runBlocking { repository.getVideoById(videoid) }
        assertEquals(v, result)

        coVerify { videoRedisRepository.insertVideoAsync(any()) }
    }

    @Test
    fun testGetVideoByIdNotFound() {
        coEvery { videoRedisRepository.getVideoById(any()) } returns null
        coEvery { videoCatalogRepository.getVideoById(any()) } returns null
        val videoid = UUID.randomUUID()
        val result = runBlocking { repository.getVideoById(videoid) }
        assertNull(result)
    }

    @Test
    fun testGetVideoPreviewAllFromCache() {
        val videoid1 = UUID.randomUUID()

        val video1 = mockVideo(videoid1)

        coEvery { videoRedisRepository.getVideoPreview(any()) } returns listOf(video1)

        val result = runBlocking { repository.getVideoPreview(listOf(videoid1)) }
        assertEquals(1, result.size)
        assertEquals(video1, result[0])
    }

    @Test
    fun testGetVideoPreview() {
        val videoid1 = UUID.randomUUID()
        val videoid2 = UUID.randomUUID()

        val video1 = mockVideo(videoid1)
        val video2 = mockVideo(videoid2)

        coEvery { videoRedisRepository.getVideoPreview(any()) } returns listOf(video1)
        coEvery { videoCatalogRepository.getVideoPreview(any()) } returns listOf(video2)
        coJustRun { videoRedisRepository.insertVideoAsync(any()) }

        val result = runBlocking { repository.getVideoPreview(listOf(videoid1, videoid2)) }
        assertEquals(2, result.size)
        assertEquals(video1, result[0])
        assertEquals(video2, result[1])

        coVerify(exactly = 1) { videoRedisRepository.insertVideoAsync(any()) }
    }

    @Test
    fun testGetUserVideosPreview() {
        val resultListPage: ResultListPage<UserVideo> = mockk()
        coEvery { videoCatalogRepository.getUserVideosPreview(any()) } returns resultListPage

        val request = mockk<GetUserVideoPreviewsRequestData>()
        val result = runBlocking { repository.getUserVideosPreview(request) }
        assertEquals(resultListPage, result)
    }

    @Test
    fun testGetLatestVideoPreviewsAsync() {
        val latestVideo: LatestVideosPage = mockk()
        coEvery { videoCatalogRepository.getLatestVideoPreviewsAsync(any()) } returns latestVideo

        val request = mockk<GetLatestVideoPreviewsRequestData>()
        val result = runBlocking { repository.getLatestVideoPreviewsAsync(request) }
        assertEquals(latestVideo, result)
    }

    private fun mockVideo(videoid: UUID): Video {
        val v = mockk<Video>()
        every { v.videoid } returns videoid
        return v
    }
}
