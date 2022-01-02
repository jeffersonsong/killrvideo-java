package com.killrvideo.service.rating.repository

import com.killrvideo.service.rating.dao.VideoRatingByUserDao
import com.killrvideo.service.rating.dao.VideoRatingDao
import com.killrvideo.service.rating.dao.VideoRatingMapper
import com.killrvideo.service.rating.dto.VideoRating
import com.killrvideo.service.rating.dto.VideoRatingByUser
import com.killrvideo.service.rating.request.GetUserRatingRequestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.CompletableFuture

internal class RatingRepositoryTest {
    private lateinit var repository: RatingRepository
    private lateinit var videoRatingDao: VideoRatingDao
    private lateinit var videoRatingByUserDao: VideoRatingByUserDao

    @BeforeEach
    fun setUp() {
        val mapper = mockk<VideoRatingMapper>()
        videoRatingDao = mockk()
        videoRatingByUserDao = mockk()
        every { mapper.videoRatingDao } returns videoRatingDao
        every { mapper.videoRatingByUserDao } returns videoRatingByUserDao
        repository = RatingRepository(mapper)
    }

    @Test
    fun testRateVideo() {
        val ratingByUser = VideoRatingByUser(videoid = UUID.randomUUID(), userid = UUID.randomUUID(), rating = 4)
        every { videoRatingDao.increment(any(), any(), any()) } returns
                CompletableFuture.completedFuture(null)

        every { videoRatingByUserDao.insert(any()) } returns
                CompletableFuture.completedFuture(ratingByUser)

        val result = runBlocking { repository.rateVideo(ratingByUser) }
        assertEquals(ratingByUser, result)
        verify {
            videoRatingDao.increment(any(), any(), any())
            videoRatingByUserDao.insert(any())
        }
    }

    @Test
    fun testFindRating() {
        val rating = VideoRating(videoid = UUID.randomUUID())
        every { videoRatingDao.findRating(any()) } returns
                CompletableFuture.completedFuture(rating)

        val result = runBlocking { repository.findRating(rating.videoid!!) }
        assertEquals(rating, result)
    }

    @Test
    fun testFindUserRating() {
        val ratingByUser = VideoRatingByUser(videoid = UUID.randomUUID(), userid = UUID.randomUUID(), rating = 4)
        every { videoRatingByUserDao.findUserRating(any(), any()) } returns
                CompletableFuture.completedFuture(ratingByUser)

        val request = getUserRatingRequestData(ratingByUser)
        val result = runBlocking { repository.findUserRating(request) }
        assertEquals(ratingByUser, result)
    }

    private fun getUserRatingRequestData(videoRatingByUser: VideoRatingByUser): GetUserRatingRequestData =
        GetUserRatingRequestData(
            videoid = videoRatingByUser.videoid!!, userid = videoRatingByUser.userid!!
        )
}
