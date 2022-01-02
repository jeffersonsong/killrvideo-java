package com.killrvideo.service.rating.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.ratings.RatingsServiceOuterClass.*
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RatingsServiceGrpcValidator {
    fun validateGrpcRequest_RateVideo(request: RateVideoRequest) {
        FluentValidator.of("rateVideo", request, LOGGER)
            .notEmpty(
                "video id",
                !request.hasVideoId() || StringUtils.isBlank(request.getVideoId().getValue())
            )
            .notEmpty(
                "user id",
                !request.hasUserId() || StringUtils.isBlank(request.getUserId().getValue())
            )
            .validate()
    }

    fun validateGrpcRequest_GetRating(request: GetRatingRequest) {
        FluentValidator.of("getRating", request, LOGGER)
            .notEmpty(
                "video id",
                !request.hasVideoId() || StringUtils.isBlank(request.getVideoId().getValue())
            )
            .validate()
    }

    fun validateGrpcRequest_GetUserRating(request: GetUserRatingRequest) {
        FluentValidator.of("getUserRating", request, LOGGER)
            .notEmpty(
                "video id",
                !request.hasVideoId() || StringUtils.isBlank(request.getVideoId().getValue())
            )
            .notEmpty(
                "user id",
                !request.hasUserId() || StringUtils.isBlank(request.getUserId().getValue())
            )
            .validate()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RatingsServiceGrpcValidator::class.java)
    }
}