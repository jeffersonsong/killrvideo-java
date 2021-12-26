package com.killrvideo.service.rating.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.killrvideo.service.rating.dao.VideoRatingByUserDao;
import com.killrvideo.service.rating.dao.VideoRatingDao;
import com.killrvideo.service.rating.dao.VideoRatingMapper;
import com.killrvideo.service.rating.dto.VideoRating;
import com.killrvideo.service.rating.dto.VideoRatingByUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
public class RatingRepository {
    private static Logger LOGGER = LoggerFactory.getLogger(RatingRepository.class);

    private VideoRatingDao videoRatingDao;
    private VideoRatingByUserDao videoRatingByUserDao;

    public RatingRepository(CqlSession session) {
        VideoRatingMapper mapper = VideoRatingMapper.build(session).build();
        this.videoRatingDao = mapper.getVideoRatingDao();
        this.videoRatingByUserDao = mapper.getVideoRatingByUserDao();
    }

    /**
     * Create a rating.
     *
     * @param videoId current videoId
     * @param userId  current userid
     * @param rating  current rating
     */
    public CompletableFuture<Void> rateVideo(UUID videoId, UUID userId, Integer rating) {
        // Param validations
        assertNotNull("rateVideo", "videoId", videoId);
        assertNotNull("rateVideo", "userId", userId);
        assertNotNull("rateVideo", "rating", rating);

        VideoRatingByUser entity = new VideoRatingByUser(videoId, userId, rating);
        // Logging at DEBUG
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Rating {} on video {} for user {}", rating, videoId, userId);
        }

        return CompletableFuture.allOf(
                videoRatingByUserDao.insert(entity),
                videoRatingDao.increment(videoId, 1L, rating)
        );
    }

    /**
     * VideoId matches the partition key set in the VideoRating class.
     *
     * @param videoId unique identifier for video.
     * @return find rating
     */
    public CompletableFuture<Optional<VideoRating>> findRating(UUID videoId) {
        assertNotNull("findRating", "videoId", videoId);

        return videoRatingDao.findRating(videoId);
    }

    /**
     * Find rating from videoid and userid.
     *
     * @param videoId current videoId
     * @param userid  current user unique identifier.
     * @return video rating is exist.
     */
    public CompletableFuture<Optional<VideoRatingByUser>> findUserRating(UUID videoId, UUID userid) {
        assertNotNull("findUserRating", "videoId", videoId);
        assertNotNull("findUserRating", "userid", userid);

        return videoRatingByUserDao.findUserRating(videoId, userid);
    }

    protected void assertNotNull(String mName, String pName, Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Assertion failed: param " + pName + " is required for method " + mName);
        }
    }
}
