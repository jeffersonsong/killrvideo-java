package com.killrvideo.service.video.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.repository.VideoCatalogRepository
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import io.grpc.Status
import io.grpc.StatusRuntimeException
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
    private lateinit var videoCatalogRepository: VideoCatalogRepository

    @MockK
    private lateinit var validator: VideoCatalogServiceGrpcValidator

    @MockK
    private lateinit var mapper: VideoCatalogServiceGrpcMapper

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testSubmitYouTubeVideoWithValidationFailure() {
        val grpcReq = createSubmitYouTubeVideoRequest()
        every { validator.validateGrpcRequest_submitYoutubeVideo(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.submitYouTubeVideo(grpcReq) }
        }
    }

    @Test
    fun testSubmitYouTubeVideoWithInsertFailure() {
        val grpcReq = createSubmitYouTubeVideoRequest()
        every { validator.validateGrpcRequest_submitYoutubeVideo(any()) } just Runs

        coEvery { videoCatalogRepository.insertVideoAsync(any()) } throws Status.INTERNAL.asRuntimeException()

        assertThrows<StatusRuntimeException> {
            runBlocking { service.submitYouTubeVideo(grpcReq) }
        }
    }

    private fun createSubmitYouTubeVideoRequest() = submitYouTubeVideoRequest {
        userId = randomUuid()
        videoId = randomUuid()
    }

    @Test
    fun testSubmitYouTubeVideoWithSendFailure() {
        val grpcReq = createSubmitYouTubeVideoRequest()
        every { validator.validateGrpcRequest_submitYoutubeVideo(any()) } just Runs
        coJustRun { videoCatalogRepository.insertVideoAsync(any()) }
        val event = YouTubeVideoAdded.getDefaultInstance()
        every { mapper.createYouTubeVideoAddedEvent(any()) } returns event
        val future: CompletableFuture<Void> = CompletableFuture.failedFuture(Exception())
        every { messagingDao.sendEvent(any(), any()) } returns future

        assertThrows<Exception> {
            runBlocking { service.submitYouTubeVideo(grpcReq) }
        }
    }

    @Test
    fun testSubmitYouTubeVideo() {
        val grpcReq = createSubmitYouTubeVideoRequest()
        every { validator.validateGrpcRequest_submitYoutubeVideo(any()) } just Runs
        coJustRun { videoCatalogRepository.insertVideoAsync(any()) }
        val event = YouTubeVideoAdded.getDefaultInstance()
        every { mapper.createYouTubeVideoAddedEvent(any()) } returns event
        every { messagingDao.sendEvent(any(), any()) } returns
                CompletableFuture.completedFuture(null)

        runBlocking { service.submitYouTubeVideo(grpcReq) }
        coVerify {
            videoCatalogRepository.insertVideoAsync(any())
        }

        verify {
            messagingDao.sendEvent(any(), any())
        }
    }

    @Test
    fun testGetLatestVideoPreviewsWithValidationFailure() {
        val grpcReq = getLatestVideoPreviewsRequest {}
        every { validator.validateGrpcRequest_getLatestPreviews(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getLatestVideoPreviews(grpcReq) }
        }
    }

    @Test
    fun testGetLatestVideoPreviewsWithQueryFailure() {
        val grpcReq = getLatestVideoPreviewsRequest {}
        every { validator.validateGrpcRequest_getLatestPreviews(any()) } just Runs
        coEvery { videoCatalogRepository.getLatestVideoPreviewsAsync(any()) } throws
                Status.INTERNAL.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getLatestVideoPreviews(grpcReq) }
        }
    }

    @Test
    fun testGetLatestVideoPreviews() {
        val grpcReq = getLatestVideoPreviewsRequest {}
        every { validator.validateGrpcRequest_getLatestPreviews(any()) } just Runs
        val returnedPage = mockk<LatestVideosPage>()
        coEvery { videoCatalogRepository.getLatestVideoPreviewsAsync(any()) } returns returnedPage
        val response = getLatestVideoPreviewsResponse {}
        every { mapper.mapLatestVideoToGrpcResponse(any()) } returns response
        runBlocking { service.getLatestVideoPreviews(grpcReq) }
    }

    @Test
    fun testGetVideoWithValidationFailure() {
        val grpcReq = getVideoRequest {}
        every { validator.validateGrpcRequest_getVideo(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getVideo(grpcReq) }
        }
    }

    @Test
    fun testGetVideoWithQueryFailure() {
        val grpcReq = createGetVideoRequest(UUID.randomUUID())
        every { validator.validateGrpcRequest_getVideo(any()) } just Runs
        coEvery { videoCatalogRepository.getVideoById(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.getVideo(grpcReq) }
        }
    }

    @Test
    fun testGetVideoWithNoVideoFound() {
        val grpcReq = createGetVideoRequest(UUID.randomUUID())
        every { validator.validateGrpcRequest_getVideo(any()) } just Runs
        coEvery { videoCatalogRepository.getVideoById(any()) } returns null
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getVideo(grpcReq) }
        }
    }

    @Test
    fun testGetVideo() {
        val grpcReq = createGetVideoRequest(UUID.randomUUID())
        every { validator.validateGrpcRequest_getVideo(any()) } just Runs
        val video = mockk<Video>()
        coEvery { videoCatalogRepository.getVideoById(any()) } returns video

        val response = GetVideoResponse.getDefaultInstance()
        every { mapper.mapFromVideotoVideoResponse(any()) } returns response
        val result = runBlocking { service.getVideo(grpcReq) }
        assertEquals(response, result)
    }

    @Test
    fun testGetVideoPreviewsWithValidationFailure() {
        val grpcReq = GetVideoPreviewsRequest.getDefaultInstance()
        every { validator.validateGrpcRequest_getVideoPreviews(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getVideoPreviews(grpcReq) }
        }
    }

    @Test
    fun testGetVideoPreviewsWithoutVideioId() {
        val grpcReq = createGetVideoPreviewsRequest()
        every { validator.validateGrpcRequest_getVideoPreviews(any()) } just Runs
        val result = runBlocking { service.getVideoPreviews(grpcReq) }
        assertEquals(0, result.videoPreviewsCount)
    }

    @Test
    fun testGetVideoPreviewsWithQueryFailure() {
        val videoid = UUID.randomUUID()
        val grpcReq = createGetVideoPreviewsRequest(videoid)
        every { validator.validateGrpcRequest_getVideoPreviews(any()) } just Runs
        coEvery { videoCatalogRepository.getVideoPreview(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.getVideoPreviews(grpcReq) }
        }
    }

    @Test
    fun testGetVideoPreviews() {
        val videoid = UUID.randomUUID()
        val grpcReq = createGetVideoPreviewsRequest(videoid)
        every { validator.validateGrpcRequest_getVideoPreviews(any()) } just Runs
        val video = mockk<Video>()
        val videos = listOf(video)
        coEvery { videoCatalogRepository.getVideoPreview(any()) } returns videos
        val response = GetVideoPreviewsResponse.getDefaultInstance()
        every { mapper.mapToGetVideoPreviewsResponse(any()) } returns response
        val result = runBlocking { service.getVideoPreviews(grpcReq) }
        assertEquals(response, result)
    }

    @Test
    fun testGetUserVideoPreviewsWithValidationFailure() {
        val grpcReq = GetUserVideoPreviewsRequest.getDefaultInstance()
        every { validator.validateGrpcRequest_getUserVideoPreviews(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getUserVideoPreviews(grpcReq) }
        }
    }

    @Test
    fun testGetUserVideoPreviewsWithQueryFailure() {
        val userid = UUID.randomUUID()
        val grpcReq = createGetUserVideoPreviewsRequest(userid)
        every { validator.validateGrpcRequest_getUserVideoPreviews(any()) } just Runs
        coEvery { videoCatalogRepository.getUserVideosPreview(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.getUserVideoPreviews(grpcReq) }
        }
    }

    @Test
    fun testGetUserVideoPreviews() {
        val userid = UUID.randomUUID()
        val grpcReq = createGetUserVideoPreviewsRequest(userid)
        every { validator.validateGrpcRequest_getUserVideoPreviews(any()) } just Runs
        val resultListPage: ResultListPage<UserVideo?> = mockk()
        coEvery { videoCatalogRepository.getUserVideosPreview(any()) } returns resultListPage
        val response = GetUserVideoPreviewsResponse.getDefaultInstance()
        every {
            mapper.mapToGetUserVideoPreviewsResponse(any(), any())
        } returns response
        val result = runBlocking { service.getUserVideoPreviews(grpcReq) }
        assertEquals(response, result)
    }

    private fun createGetVideoRequest(videoid: UUID): GetVideoRequest =
        getVideoRequest {
            videoId = uuidToUuid(videoid)
        }

    private fun createGetVideoPreviewsRequest(vararg videoids: UUID): GetVideoPreviewsRequest =
        getVideoPreviewsRequest {
            Arrays.stream(videoids).map { uuid: UUID? -> uuidToUuid(uuid) }
                .forEach { videoIds.add(it) }
        }

    private fun createGetUserVideoPreviewsRequest(userid: UUID): GetUserVideoPreviewsRequest =
        getUserVideoPreviewsRequest {
            userId = uuidToUuid(userid)
        }

    private fun createGetUserVideoPreviewsRequestData(userid: UUID): GetUserVideoPreviewsRequestData =
        GetUserVideoPreviewsRequestData(userid)

}
