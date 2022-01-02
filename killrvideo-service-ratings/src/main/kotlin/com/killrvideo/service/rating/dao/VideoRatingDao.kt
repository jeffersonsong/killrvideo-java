package com.killrvideo.service.rating.dao;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Increment;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes;
import com.killrvideo.service.rating.dto.VideoRating;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Dao
public interface VideoRatingDao {
    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<Optional<VideoRating>> findRating(UUID videoId);

    @Increment(entityClass = VideoRating.class)
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    CompletableFuture<Void> increment(UUID videoid, long ratingCounter, long ratingTotal);
}
