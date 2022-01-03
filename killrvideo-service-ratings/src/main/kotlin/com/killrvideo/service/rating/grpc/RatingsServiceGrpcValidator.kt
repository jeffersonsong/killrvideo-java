package com.killrvideo.service.rating.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.ratings.RatingsServiceOuterClass.*
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@Component
class RatingsServiceGrpcValidator {
    private val logger = KotlinLogging.logger {  }

    fun validateGrpcRequest_RateVideo(request: RateVideoRequest) =
        FluentValidator.of("rateVideo", request, logger)
            .notEmpty(
                "video id",
                !request.hasVideoId() || StringUtils.isBlank(request.videoId.value)
            )
            .notEmpty(
                "user id",
                !request.hasUserId() || StringUtils.isBlank(request.userId.value)
            )
            .validate()

    fun validateGrpcRequest_GetRating(request: GetRatingRequest) =
        FluentValidator.of("getRating", request, logger)
            .notEmpty(
                "video id",
                !request.hasVideoId() || StringUtils.isBlank(request.videoId.value)
            )
            .validate()

    fun validateGrpcRequest_GetUserRating(request: GetUserRatingRequest) =
        FluentValidator.of("getUserRating", request, logger)
            .notEmpty(
                "video id",
                !request.hasVideoId() || StringUtils.isBlank(request.videoId.value)
            )
            .notEmpty(
                "user id",
                !request.hasUserId() || StringUtils.isBlank(request.userId.value)
            )
            .validate()
}
