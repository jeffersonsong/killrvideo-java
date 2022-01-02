package com.killrvideo.service.rating.dao

import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Increment
import com.datastax.oss.driver.api.mapper.annotations.Select
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes
import com.killrvideo.service.rating.dto.VideoRating
import java.util.*
import java.util.concurrent.CompletableFuture

@Dao
interface VideoRatingDao {
    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun findRating(videoId: UUID): CompletableFuture<VideoRating?>

    @Increment(entityClass = [VideoRating::class])
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun increment(videoid: UUID, ratingCounter: Long, ratingTotal: Long): CompletableFuture<Void>
}
