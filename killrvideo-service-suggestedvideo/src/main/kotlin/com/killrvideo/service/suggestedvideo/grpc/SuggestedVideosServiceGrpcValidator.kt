package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcValidator
import com.killrvideo.utils.FluentValidator
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SuggestedVideosServiceGrpcValidator {
    fun validateGrpcRequest_getRelatedVideo(request: GetRelatedVideosRequest) {
        FluentValidator.of("getRelatedVideo", request, LOGGER)
            .notEmpty("video id", StringUtils.isBlank(request.videoId.value))
            .validate()
    }

    fun validateGrpcRequest_getUserSuggestedVideo(request: GetSuggestedForUserRequest) {
        FluentValidator.of("getSuggestedForUser", request, LOGGER)
            .notEmpty("user id", StringUtils.isBlank(request.userId.value))
            .validate()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SuggestedVideosServiceGrpcValidator::class.java)
    }
}