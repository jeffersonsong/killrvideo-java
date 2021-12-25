package com.killrvideo.service.rating.dao;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes;
import com.killrvideo.service.rating.dto.VideoRatingByUser;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Dao
public interface VideoRatingByUserDao {
    @Insert
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    CompletableFuture<Void> insert(VideoRatingByUser videoRatingByUser);

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<Optional<VideoRatingByUser>> findUserRating(UUID videoId, UUID userid);
}
