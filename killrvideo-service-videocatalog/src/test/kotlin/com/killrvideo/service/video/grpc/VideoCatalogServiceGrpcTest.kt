package com.killrvideo.service.video.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.exception.NotFoundException
import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.repository.CachedVideoCatalogRepository
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import killrvideo.video_catalog.*
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import java.util.concurrent.CompletableFuture

internal class VideoCatalogServiceGrpcTest {
    @InjectMockKs
    private lateinit var service: VideoCatalogServiceGrpc

    @MockK
    private lateinit var messagingDao: MessagingDao

    @MockK
    private lateinit var videoCatalogRepository: CachedVideoCatalogRepository

    @MockK
    private lateinit var validator: VideoCatalogServiceGrpcValidator

    @MockK
    private lateinit var mapper: VideoCatalogServiceGrpcMapper

    val serviceKey ="VideoCatalogService"
    private val topicVideoCreated = "topic-kv-videoCreation"

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testSubmitYouTubeVideoWithValidationFailure() {
        val request = createSubmitYouTubeVideoRequest()
        every { validator.validateGrpcRequest_submitYoutubeVideo(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.submitYouTubeVideo(request) }
        }
    }

    @Test
    fun testSubmitYouTubeVideoWithInsertFailure() {
        val request = createSubmitYouTubeVideoRequest()
        every { validator.validateGrpcRequest_submitYoutubeVideo(any()) } just Runs

        coEvery { videoCatalogRepository.insertVideoAsync(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.submitYouTubeVideo(request) }
        }
    }

    private fun createSubmitYouTubeVideoRequest() = submitYouTubeVideoRequest {
        userId = randomUuid()
        videoId = randomUuid()
    }

    @Test
    fun testSubmitYouTubeVideoWithSendFailure() {
        val request = createSubmitYouTubeVideoRequest()
        every { validator.validateGrpcRequest_submitYoutubeVideo(any()) } just Runs
        coJustRun { videoCatalogRepository.insertVideoAsync(any()) }
        val event = YouTubeVideoAdded.getDefaultInstance()
        every { mapper.createYouTubeVideoAddedEvent(any()) } returns event
        val future: CompletableFuture<Void> = CompletableFuture.failedFuture(Exception())
        every { messagingDao.sendEvent(any(), any()) } returns future

        assertThrows<Exception> {
            runBlocking { service.submitYouTubeVideo(request) }
        }
    }

    @Test
    fun testSubmitYouTubeVideo() {
        val request = createSubmitYouTubeVideoRequest()
        every { validator.validateGrpcRequest_submitYoutubeVideo(any()) } just Runs
        coJustRun { videoCatalogRepository.insertVideoAsync(any()) }
        val event = YouTubeVideoAdded.getDefaultInstance()
        every { mapper.createYouTubeVideoAddedEvent(any()) } returns event
        every { messagingDao.sendEvent(any(), any()) } returns
                CompletableFuture.completedFuture(null)

        runBlocking { service.submitYouTubeVideo(request) }
        coVerify {
            videoCatalogRepository.insertVideoAsync(any())
        }

        verify {
            messagingDao.sendEvent(any(), any())
        }
    }

    @Test
    fun testGetLatestVideoPreviewsWithValidationFailure() {
        val request = getLatestVideoPreviewsRequest {}
        every { validator.validateGrpcRequest_getLatestPreviews(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getLatestVideoPreviews(request) }
        }
    }

    @Test
    fun testGetLatestVideoPreviewsWithQueryFailure() {
        val request = getLatestVideoPreviewsRequest {}
        every { validator.validateGrpcRequest_getLatestPreviews(any()) } just Runs
        coEvery { videoCatalogRepository.getLatestVideoPreviewsAsync(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.getLatestVideoPreviews(request) }
        }
    }

    @Test
    fun testGetLatestVideoPreviews() {
        val request = getLatestVideoPreviewsRequest {}
        every { validator.validateGrpcRequest_getLatestPreviews(any()) } just Runs
        val returnedPage = mockk<LatestVideosPage>()
        coEvery { videoCatalogRepository.getLatestVideoPreviewsAsync(any()) } returns returnedPage
        val response = getLatestVideoPreviewsResponse {}
        every { mapper.mapLatestVideoToGrpcResponse(any()) } returns response
        runBlocking { service.getLatestVideoPreviews(request) }
    }

    @Test
    fun testGetVideoWithValidationFailure() {
        val request = getVideoRequest {}
        every { validator.validateGrpcRequest_getVideo(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getVideo(request) }
        }
    }

    @Test
    fun testGetVideoWithQueryFailure() {
        val request = createGetVideoRequest(UUID.randomUUID())
        every { validator.validateGrpcRequest_getVideo(any()) } just Runs
        coEvery { videoCatalogRepository.getVideoById(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.getVideo(request) }
        }
    }

    @Test
    fun testGetVideoWithNoVideoFound() {
        val request = createGetVideoRequest(UUID.randomUUID())
        every { validator.validateGrpcRequest_getVideo(any()) } just Runs
        coEvery { videoCatalogRepository.getVideoById(any()) } returns null
        assertThrows<NotFoundException> {
            runBlocking { service.getVideo(request) }
        }
    }

    @Test
    fun testGetVideo() {
        val request = createGetVideoRequest(UUID.randomUUID())
        every { validator.validateGrpcRequest_getVideo(any()) } just Runs
        val video = mockk<Video>()
        coEvery { videoCatalogRepository.getVideoById(any()) } returns video

        val response = GetVideoResponse.getDefaultInstance()
        every { mapper.mapFromVideotoVideoResponse(any()) } returns response
        val result = runBlocking { service.getVideo(request) }
        assertEquals(response, result)
    }

    @Test
    fun testGetVideoPreviewsWithValidationFailure() {
        val request = GetVideoPreviewsRequest.getDefaultInstance()
        every { validator.validateGrpcRequest_getVideoPreviews(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getVideoPreviews(request) }
        }
    }

    @Test
    fun testGetVideoPreviewsWithoutVideioId() {
        val request = createGetVideoPreviewsRequest()
        every { validator.validateGrpcRequest_getVideoPreviews(any()) } just Runs
        val result = runBlocking { service.getVideoPreviews(request) }
        assertEquals(0, result.videoPreviewsCount)
    }

    @Test
    fun testGetVideoPreviewsWithQueryFailure() {
        val videoid = UUID.randomUUID()
        val request = createGetVideoPreviewsRequest(videoid)
        every { validator.validateGrpcRequest_getVideoPreviews(any()) } just Runs
        coEvery { videoCatalogRepository.getVideoPreview(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.getVideoPreviews(request) }
        }
    }

    @Test
    fun testGetVideoPreviews() {
        val videoid = UUID.randomUUID()
        val request = createGetVideoPreviewsRequest(videoid)
        every { validator.validateGrpcRequest_getVideoPreviews(any()) } just Runs
        val video = mockk<Video>()
        val videos = listOf(video)
        coEvery { videoCatalogRepository.getVideoPreview(any()) } returns videos
        val response = GetVideoPreviewsResponse.getDefaultInstance()
        every { mapper.mapToGetVideoPreviewsResponse(any()) } returns response
        val result = runBlocking { service.getVideoPreviews(request) }
        assertEquals(response, result)
    }

    @Test
    fun testGetUserVideoPreviewsWithValidationFailure() {
        val request = GetUserVideoPreviewsRequest.getDefaultInstance()
        every { validator.validateGrpcRequest_getUserVideoPreviews(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getUserVideoPreviews(request) }
        }
    }

    @Test
    fun testGetUserVideoPreviewsWithQueryFailure() {
        val userid = UUID.randomUUID()
        val request = createGetUserVideoPreviewsRequest(userid)
        every { validator.validateGrpcRequest_getUserVideoPreviews(any()) } just Runs
        coEvery { videoCatalogRepository.getUserVideosPreview(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.getUserVideoPreviews(request) }
        }
    }

    @Test
    fun testGetUserVideoPreviews() {
        val userid = UUID.randomUUID()
        val request = createGetUserVideoPreviewsRequest(userid)
        every { validator.validateGrpcRequest_getUserVideoPreviews(any()) } just Runs
        val resultListPage: ResultListPage<UserVideo> = mockk()
        coEvery { videoCatalogRepository.getUserVideosPreview(any()) } returns resultListPage
        val response = GetUserVideoPreviewsResponse.getDefaultInstance()
        every {
            mapper.mapToGetUserVideoPreviewsResponse(any(), any())
        } returns response
        val result = runBlocking { service.getUserVideoPreviews(request) }
        assertEquals(response, result)
    }

    private fun createGetVideoRequest(videoid: UUID): GetVideoRequest =
        getVideoRequest {
            videoId = uuidToUuid(videoid)
        }

    private fun createGetVideoPreviewsRequest(vararg videoids: UUID): GetVideoPreviewsRequest =
        getVideoPreviewsRequest {
            Arrays.stream(videoids).map { uuid: UUID -> uuidToUuid(uuid) }
                .forEach { videoIds.add(it) }
        }

    private fun createGetUserVideoPreviewsRequest(userid: UUID): GetUserVideoPreviewsRequest =
        getUserVideoPreviewsRequest {
            userId = uuidToUuid(userid)
        }

    private fun createGetUserVideoPreviewsRequestData(userid: UUID): GetUserVideoPreviewsRequestData =
        GetUserVideoPreviewsRequestData(userid)

}
