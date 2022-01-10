package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils.isBlank
import org.springframework.stereotype.Component

@Component
class SuggestedVideosServiceGrpcValidator {
    private val logger = KotlinLogging.logger { }

    fun validateGrpcRequest_getRelatedVideo(request: GetRelatedVideosRequest) =
        FluentValidator.of("getRelatedVideo", request, logger)
            .notEmpty("video id", isBlank(request.videoId.value))
            .validate()

    fun validateGrpcRequest_getUserSuggestedVideo(request: GetSuggestedForUserRequest) =
        FluentValidator.of("getSuggestedForUser", request, logger)
            .notEmpty("user id", isBlank(request.userId.value))
            .validate()
}
