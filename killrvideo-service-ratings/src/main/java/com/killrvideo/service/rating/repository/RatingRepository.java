package com.killrvideo.service.rating.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
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
import java.util.concurrent.CompletionStage;

@Repository
public class RatingRepository {
    private static Logger LOGGER = LoggerFactory.getLogger(RatingRepository.class);

    private CqlSession session;
    private VideoRatingDao videoRatingDao;
    private VideoRatingByUserDao videoRatingByUserDao;
    /** Precompile statements to speed up queries. */
    private PreparedStatement updateRating;

    public RatingRepository(CqlSession session) {
        this.session = session;
        VideoRatingMapper mapper = VideoRatingMapper.build(session).build();
        this.videoRatingDao = mapper.getVideoRatingDao();
        this.videoRatingByUserDao = mapper.getVideoRatingByUserDao();
        SimpleStatement updateStatement = QueryBuilder.update("video_ratings")
                .increment(VideoRating.COLUMN_RATING_COUNTER)
                .increment(VideoRating.COLUMN_RATING_TOTAL, QueryBuilder.bindMarker())
                .whereColumn(VideoRating.COLUMN_VIDEOID).isEqualTo(QueryBuilder.bindMarker())
                .build();
        updateStatement.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        updateRating = session.prepare(updateStatement);
    }

    public CompletableFuture<Void> rateVideo(UUID videoId, UUID userId, Integer rating) {
        // Param validations
        assertNotNull("rateVideo", "videoId", videoId);
        assertNotNull("rateVideo", "userId", userId);
        assertNotNull("rateVideo", "rating", rating);

        BoundStatement statement = updateRating.bind()
                .setLong(VideoRating.COLUMN_RATING_TOTAL, rating)
                .setUuid(VideoRating.COLUMN_VIDEOID,      videoId);

        VideoRatingByUser entity = new VideoRatingByUser(videoId, userId, rating);
        // Logging at DEBUG
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Rating {} on video {} for user {}", rating, videoId, userId);
        }

        CompletionStage<AsyncResultSet> future1 = session.executeAsync(statement);
        CompletableFuture<Void> future2 = videoRatingByUserDao.insert(entity);

        return CompletableFuture.allOf(
                future1.toCompletableFuture().thenApply(s-> null),
                future2);
    }

    /**
     * VideoId matches the partition key set in the VideoRating class.
     *
     * @param videoId
     *      unique identifier for video.
     * @return
     *      find rating
     */
    public CompletableFuture<Optional< VideoRating >> findRating(UUID videoId) {
        assertNotNull("findRating", "videoId", videoId);

        return videoRatingDao.findRating(videoId);
    }

    /**
     * Find rating from videoid and userid.
     *
     * @param videoId
     *      current videoId
     * @param userid
     *      current user unique identifier.
     * @return
     *      video rating is exist.
     */
    public CompletableFuture< Optional < VideoRatingByUser > > findUserRating(UUID videoId, UUID userid) {
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
