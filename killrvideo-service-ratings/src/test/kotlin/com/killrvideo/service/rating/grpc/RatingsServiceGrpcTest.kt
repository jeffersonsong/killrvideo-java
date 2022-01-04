package com.killrvideo.service.rating.grpc

import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.rating.dto.VideoRating
import com.killrvideo.service.rating.dto.VideoRatingByUser
import com.killrvideo.service.rating.repository.RatingRepository
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import killrvideo.ratings.*
import killrvideo.ratings.events.userRatedVideo
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    private val serviceKey = "RatingsService"
    private val topicvideoRated = "topic-kv-videoRating"

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testRateVideoWithValidationFailed() {
        val request = rateVideoRequest {}
        every { validator.validateGrpcRequest_RateVideo(any())} throws IllegalArgumentException()
        
        assertThrows<IllegalArgumentException> {
            runBlocking { service.rateVideo(request) }
        }
    }

    @Test
    fun testRateVideoWithInsertFailed() {
        val request = rateVideoRequest {
            videoId=randomUuid()
            userId=randomUuid()
            rating =4
        }
        every {validator.validateGrpcRequest_RateVideo(any()) } just Runs
        coEvery { ratingRepository.rateVideo(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.rateVideo(request) }
        }
    }

    @Test
    fun testRateVideo() {
        val request = rateVideoRequest {
            videoId=randomUuid()
            userId=randomUuid()
            rating =4
        }
        every {validator.validateGrpcRequest_RateVideo(any()) } just Runs
        val rating = mockk<VideoRatingByUser>()
        val event = userRatedVideo {}
        every { mapper.createUserRatedVideoEvent(any()) } returns event
        coEvery {ratingRepository.rateVideo(any()) } returns rating
        every {messagingDao.sendEvent(any(), any())} returns
            CompletableFuture.completedFuture(null)

        runBlocking { service.rateVideo(request) }
    }

    @Test
    fun testGetRating() {
        val request = getRatingRequest {}
        every { validator.validateGrpcRequest_GetRating(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getRating(request) }
        }
    }

    @Test
    fun testGetRatingWithQueryFailed() {
        val request = getRatingRequest {videoId= randomUuid() }
        every {validator.validateGrpcRequest_GetRating(any())} just Runs
        coEvery { ratingRepository.findRating(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.getRating(request) }
        }
    }

    @Test
    fun testGetRatingWithRatingPresent() {
        val request = getRatingRequest {videoId= randomUuid() }
        every {validator.validateGrpcRequest_GetRating(any())} just Runs
        val rating = mockk<VideoRating>()
        val response = getRatingResponse {}
        every { mapper.mapToRatingResponse(any()) } returns response
        coEvery { ratingRepository.findRating(any()) } returns rating

        val result = runBlocking { service.getRating(request) }
        assertEquals(response, result)
    }

    @Test
    fun testGetUserRatingWithValidationFailed() {
        val request = getUserRatingRequest {}
        every { validator.validateGrpcRequest_GetUserRating(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException>{
            runBlocking { service.getUserRating(request) }
        }
    }

    @Test
    fun testGetUserRatingWithQueryFailed() {
        val request = getUserRatingRequest {
            videoId=randomUuid()
            userId=randomUuid()
        }
        every { validator.validateGrpcRequest_GetUserRating(any()) } just Runs
        coEvery { ratingRepository.findUserRating(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.getUserRating(request) }
        }
    }

    @Test
    fun testGetUserRatingWithUserRatingPresent() {
        val videoid = UUID.randomUUID()
        val userid = UUID.randomUUID()

        val request = getUserRatingRequest {
            videoId = uuidToUuid(videoid)
            userId = uuidToUuid(userid)
        }

        every {validator.validateGrpcRequest_GetUserRating(any()) } just Runs

        val rating = mockk<VideoRatingByUser>()
        val response = getUserRatingResponse {}
        every {mapper.mapToUserRatingResponse(any()) } returns response

        coEvery {ratingRepository.findUserRating(any())} returns rating

        val result = runBlocking { service.getUserRating(request) }
        assertEquals(response, result)
    }
}
