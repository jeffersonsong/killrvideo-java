package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.utils.GrpcMappingUtils
import io.grpc.StatusRuntimeException
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SuggestedVideosServiceGrpcValidatorTest {
    private val validator = SuggestedVideosServiceGrpcValidator()

    @Test
    fun testValidateGrpcRequest_getRelatedVideo_Success() {
        val request = GetRelatedVideosRequest.newBuilder()
            .setVideoId(GrpcMappingUtils.randomUuid())
            .build()
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
        val request = GetSuggestedForUserRequest.newBuilder()
            .setUserId(GrpcMappingUtils.randomUuid())
            .build()
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