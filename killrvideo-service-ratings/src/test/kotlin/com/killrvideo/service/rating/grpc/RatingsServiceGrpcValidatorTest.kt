package com.killrvideo.service.rating.grpc

import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import killrvideo.ratings.getRatingRequest
import killrvideo.ratings.getUserRatingRequest
import killrvideo.ratings.rateVideoRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RatingsServiceGrpcValidatorTest {
    private val validator = RatingsServiceGrpcValidator()

    @Test
    fun testValidateGrpcRequest_RateVideo_Success() {
        val request = rateVideoRequest {
            videoId = randomUuid()
            userId = randomUuid()
        }
        validator.validateGrpcRequest_RateVideo(request)
    }

    @Test
    fun testValidateGrpcRequest_RateVideo_Failure() {
        val request = rateVideoRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_RateVideo(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_GetRating_Success() {
        val request = getRatingRequest {
            videoId = randomUuid()
        }
        validator.validateGrpcRequest_GetRating(request)
    }

    @Test
    fun testValidateGrpcRequest_GetRating_Failure() {
        val request = getRatingRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_GetRating(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_GetUserRating_Success() {
        val request = getUserRatingRequest {
            videoId = randomUuid()
            userId = randomUuid()
        }
        validator.validateGrpcRequest_GetUserRating(request)
    }

    @Test
    fun testValidateGrpcRequest_GetUserRating_Failure() {
        val request = getUserRatingRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_GetUserRating(request)
        }
    }
}
