package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import io.grpc.StatusRuntimeException
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest
import killrvideo.suggested_videos.getRelatedVideosRequest
import killrvideo.suggested_videos.getSuggestedForUserRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SuggestedVideosServiceGrpcValidatorTest {
    private val validator = SuggestedVideosServiceGrpcValidator()

    @Test
    fun testValidateGrpcRequest_getRelatedVideo_Success() {
        val request = getRelatedVideosRequest {
            videoId = randomUuid()
        }
        validator.validateGrpcRequest_getRelatedVideo(request)
    }

    @Test
    fun testValidateGrpcRequest_getRelatedVideo_Failure() {
        val request = GetRelatedVideosRequest.getDefaultInstance()
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequest_getRelatedVideo(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_getUserSuggestedVideo_Success() {
        val request = getSuggestedForUserRequest {
            userId = randomUuid()
        }
        validator.validateGrpcRequest_getUserSuggestedVideo(request)
    }

    @Test
    fun testValidateGrpcRequest_getUserSuggestedVideo_Failure() {
        val request = GetSuggestedForUserRequest.getDefaultInstance()
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequest_getUserSuggestedVideo(request)
        }
    }
}