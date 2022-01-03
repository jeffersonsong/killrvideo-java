package com.killrvideo.service.rating.grpc

import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.rating.dto.VideoRating
import com.killrvideo.service.rating.dto.VideoRatingByUser
import com.killrvideo.service.rating.repository.RatingRepository
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import killrvideo.ratings.*
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Value
import java.util.*
import java.util.concurrent.CompletableFuture

internal class RatingsServiceGrpcTest {
    @InjectMockKs
    private lateinit var service: RatingsServiceGrpc

    @MockK
    private lateinit var messagingDao: MessagingDao

    @MockK
    private lateinit var ratingRepository: RatingRepository

    @MockK
    private lateinit var validator: RatingsServiceGrpcValidator

    @MockK
    private lateinit var mapper: RatingsServiceGrpcMapper
    private val serviceKey = "RatingsService";
    private val topicvideoRated = "topic-kv-videoRating"

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testRateVideoWithValidationFailed() {
        val grpcReq = rateVideoRequest {}
        every { validator.validateGrpcRequest_RateVideo(any())} throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        
        assertThrows<StatusRuntimeException> {
            runBlocking { service.rateVideo(grpcReq) }
        }
    }

    @Test
    fun testRateVideoWithInsertFailed() {
        val grpcReq = rateVideoRequest {
            videoId=randomUuid()
            userId=randomUuid()
            rating =4
        }
        every {validator.validateGrpcRequest_RateVideo(any()) } just Runs
        coEvery { ratingRepository.rateVideo(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.rateVideo(grpcReq) }
        }
    }

    @Test
    fun testRateVideo() {
        val grpcReq = rateVideoRequest {
            videoId=randomUuid()
            userId=randomUuid()
            rating =4
        }
        every {validator.validateGrpcRequest_RateVideo(any()) } just Runs
        val rating = mockk<VideoRatingByUser>()
        val event = UserRatedVideo.getDefaultInstance()
        every { mapper.createUserRatedVideoEvent(any()) } returns event
        coEvery {ratingRepository.rateVideo(any()) } returns rating
        every {messagingDao.sendEvent(any(), any())} returns
            CompletableFuture.completedFuture(null)

        runBlocking { service.rateVideo(grpcReq) }
    }

    @Test
    fun testGetRating() {
        val grpcReq = getRatingRequest {}
        every { validator.validateGrpcRequest_GetRating(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getRating(grpcReq) }
        }
    }

    @Test
    fun testGetRatingWithQueryFailed() {
        val grpcReq = getRatingRequest {videoId= randomUuid() }
        every {validator.validateGrpcRequest_GetRating(any())} just Runs
        coEvery { ratingRepository.findRating(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.getRating(grpcReq) }
        }
    }

    @Test
    fun testGetRatingWithRatingPresent() {
        val grpcReq = getRatingRequest {videoId= randomUuid() }
        every {validator.validateGrpcRequest_GetRating(any())} just Runs
        val rating = mockk<VideoRating>()
        val response = getRatingResponse {}
        every { mapper.mapToRatingResponse(any()) } returns response
        coEvery { ratingRepository.findRating(any()) } returns rating

        val result = runBlocking { service.getRating(grpcReq) }
        assertEquals(response, result)
    }

    @Test
    fun testGetRatingWithRatingAbsent() {
        val grpcReq = getRatingRequest {videoId= randomUuid() }
        every {validator.validateGrpcRequest_GetRating(any())} just Runs
        coEvery { ratingRepository.findRating(any()) } returns null

        val result = runBlocking { service.getRating(grpcReq) }
        assertEquals(0L, result.ratingsTotal)
        assertEquals(0L, result.ratingsCount)
    }

    @Test
    fun testGetUserRatingWithValidationFailed() {
        val grpcReq = getUserRatingRequest {}
        every { validator.validateGrpcRequest_GetUserRating(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException>{
            runBlocking { service.getUserRating(grpcReq) }
        }
    }

    @Test
    fun testGetUserRatingWithQueryFailed() {
        val grpcReq = getUserRatingRequest {
            videoId=randomUuid()
            userId=randomUuid()
        }
        every { validator.validateGrpcRequest_GetUserRating(any()) } just Runs
        coEvery { ratingRepository.findUserRating(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.getUserRating(grpcReq) }
        }
    }

    @Test
    fun testGetUserRatingWithUserRatingPresent() {
        val videoid = UUID.randomUUID()
        val userid = UUID.randomUUID()

        val grpcReq = getUserRatingRequest {
            videoId = uuidToUuid(videoid)
            userId = uuidToUuid(userid)
        }

        every {validator.validateGrpcRequest_GetUserRating(any()) } just Runs

        val rating = mockk<VideoRatingByUser>()
        val response = getUserRatingResponse {}
        every {mapper.mapToUserRatingResponse(any()) } returns response

        coEvery {ratingRepository.findUserRating(any())} returns rating

        val result = runBlocking { service.getUserRating(grpcReq) }
        assertEquals(response, result)
    }

    @Test
    fun testGetUserRatingWithUserRatingAbsent() {
        val videoid = UUID.randomUUID()
        val userid = UUID.randomUUID()
        val grpcReq = getUserRatingRequest {
            videoId = uuidToUuid(videoid)
            userId = uuidToUuid(userid)
        }
        every {validator.validateGrpcRequest_GetUserRating(any()) } just Runs
        coEvery { ratingRepository.findUserRating(any())} returns null
        val result = runBlocking { service.getUserRating(grpcReq) }
        assertEquals(0, result.rating)
    }
}
