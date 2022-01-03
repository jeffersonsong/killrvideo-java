package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.suggestedvideo.dto.Video
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import killrvideo.suggested_videos.SuggestedVideosService.*
import killrvideo.suggested_videos.getRelatedVideosRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Value
import java.util.*

internal class SuggestedVideosServiceGrpcTest {
    @InjectMockKs
    private lateinit var service: SuggestedVideosServiceGrpc

    @MockK
    private lateinit var suggestedVideosRepository: SuggestedVideosRepository

    @MockK
    private lateinit var validator: SuggestedVideosServiceGrpcValidator

    @MockK
    private lateinit var mapper: SuggestedVideosServiceGrpcMapper
    val serviceKey = "SuggestedVideoService"

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    fun createGetRelatedVideosRequest(videoid: UUID, pagesize: Int, pagingstate: String): GetRelatedVideosRequest {
        return getRelatedVideosRequest {
            videoId = uuidToUuid(videoid)
            pageSize = pagesize
            pagingState = pagingstate
        }
    }

    @Test
    fun testGetRelatedVideosWithValidationFailure() {
        val grpcReq = GetRelatedVideosRequest.getDefaultInstance()
        every { validator.validateGrpcRequest_getRelatedVideo(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getRelatedVideos(grpcReq) }
        }
    }

    @Test
    fun testGetRelatedVideosWithQueryFailure() {
        val grpcReq = createGetRelatedVideosRequest(UUID.randomUUID(), 5, "")
        every { validator.validateGrpcRequest_getRelatedVideo(any()) } just Runs
        coEvery {
            suggestedVideosRepository.getRelatedVideos(any())
        } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.getRelatedVideos(grpcReq) }
        }
    }

    @Test
    fun testGetRelatedVideos() {
        val grpcReq = createGetRelatedVideosRequest(UUID.randomUUID(), 5, "")
        every { validator.validateGrpcRequest_getRelatedVideo(any()) } just Runs
        val resultListPage: ResultListPage<Video> = mockk()
        val response = GetRelatedVideosResponse.getDefaultInstance()
        every { mapper.mapToGetRelatedVideosResponse(any(), any()) } returns response
        coEvery { suggestedVideosRepository.getRelatedVideos(any()) } returns resultListPage

        val result = runBlocking { service.getRelatedVideos(grpcReq) }
        assertEquals(response, result)
    }

    @Test
    fun testGetSuggestedForUserWithValidationFailure() {
        val grpcReq = GetSuggestedForUserRequest.getDefaultInstance()
        every {validator.validateGrpcRequest_getUserSuggestedVideo(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getSuggestedForUser(grpcReq) }
        }
    }
}
