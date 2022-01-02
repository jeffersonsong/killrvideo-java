package com.killrvideo.service.rating.repository

import com.killrvideo.service.rating.dao.VideoRatingByUserDao
import com.killrvideo.service.rating.dao.VideoRatingDao
import com.killrvideo.service.rating.dao.VideoRatingMapper
import com.killrvideo.service.rating.dto.VideoRating
import com.killrvideo.service.rating.dto.VideoRatingByUser
import com.killrvideo.service.rating.request.GetUserRatingRequestData
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
class RatingRepository(mapper: VideoRatingMapper) {
    private val videoRatingDao: VideoRatingDao = mapper.videoRatingDao
    private val videoRatingByUserDao: VideoRatingByUserDao = mapper.videoRatingByUserDao

    /**
     * Create a rating.
     *
     * @param videoRatingByUser video rating by user.
     */
    suspend fun rateVideo(videoRatingByUser: VideoRatingByUser): VideoRatingByUser? {
        assertNotNull("rateVideo", "videoId", videoRatingByUser.videoid)
        // Logging at DEBUG
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug(
                "Rating {} on video {} for user {}", videoRatingByUser.rating,
                videoRatingByUser.videoid, videoRatingByUser.userid
            )
        }
        return videoRatingDao.increment(
            videoRatingByUser.videoid!!, 1L,
            videoRatingByUser.rating.toLong()
        )
            .thenCompose { _ -> videoRatingByUserDao.insert(videoRatingByUser) }
            .await()
    }

    /**
     * VideoId matches the partition key set in the VideoRating class.
     *
     * @param videoId unique identifier for video.
     * @return find rating
     */
    suspend fun findRating(videoId: UUID): VideoRating? =
        videoRatingDao.findRating(videoId).await()

    /**
     * Find rating from videoid and userid.
     *
     * @param request request.
     * @return video rating exists.
     */
    suspend fun findUserRating(request: GetUserRatingRequestData): VideoRatingByUser? =
        videoRatingByUserDao.findUserRating(request.videoid, request.userid).await()

    private fun assertNotNull(mName: String, pName: String, obj: Any?) =
        requireNotNull(obj) { "Assertion failed: param $pName is required for method $mName" }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RatingRepository::class.java)
    }
}