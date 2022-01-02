package com.killrvideo.service.rating.dao

import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Insert
import com.datastax.oss.driver.api.mapper.annotations.Select
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes
import com.killrvideo.service.rating.dto.VideoRatingByUser
import java.util.*
import java.util.concurrent.CompletableFuture

@Dao
interface VideoRatingByUserDao {
    @Insert
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun insert(videoRatingByUser: VideoRatingByUser): CompletableFuture<VideoRatingByUser?>

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun findUserRating(videoId: UUID, userid: UUID): CompletableFuture<VideoRatingByUser?>
}
