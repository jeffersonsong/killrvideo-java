package com.killrvideo.service.rating.repository;

import com.killrvideo.service.rating.dao.VideoRatingByUserDao;
import com.killrvideo.service.rating.dao.VideoRatingDao;
import com.killrvideo.service.rating.dao.VideoRatingMapper;
import com.killrvideo.service.rating.dto.VideoRating;
import com.killrvideo.service.rating.dto.VideoRatingByUser;
import com.killrvideo.service.rating.request.GetUserRatingRequestData;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(RatingRepository.class);

    private final VideoRatingDao videoRatingDao;
    private final VideoRatingByUserDao videoRatingByUserDao;

    public RatingRepository(VideoRatingMapper mapper) {
        this.videoRatingDao = mapper.getVideoRatingDao();
        this.videoRatingByUserDao = mapper.getVideoRatingByUserDao();
    }

    /**
     * Create a rating.
     *
     * @param videoRatingByUser video rating by user.
     */
    public CompletableFuture<VideoRatingByUser> rateVideo(VideoRatingByUser videoRatingByUser) {
        // Logging at DEBUG
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Rating {} on video {} for user {}", videoRatingByUser.getRating(),
                    videoRatingByUser.getVideoid(), videoRatingByUser.getUserid());
        }

        return videoRatingDao.increment(videoRatingByUser.getVideoid(), 1L,
                        videoRatingByUser.getRating())
                .thenCompose(rs -> videoRatingByUserDao.insert(videoRatingByUser));
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
     * @param request request.
     * @return video rating exists.
     */
    public CompletableFuture<Optional<VideoRatingByUser>> findUserRating(GetUserRatingRequestData request) {
        assertNotNull("findUserRating", "videoId", request.getVideoid());
        assertNotNull("findUserRating", "userid", request.getUserid());

        return videoRatingByUserDao.findUserRating(request.getVideoid(), request.getUserid());
    }

    private void assertNotNull(String mName, String pName, Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Assertion failed: param " + pName + " is required for method " + mName);
        }
    }
}
