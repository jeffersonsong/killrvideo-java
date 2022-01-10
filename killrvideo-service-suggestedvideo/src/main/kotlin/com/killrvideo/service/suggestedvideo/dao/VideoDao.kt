package com.killrvideo.service.suggestedvideo.dao

import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Select
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes
import com.killrvideo.service.suggestedvideo.dto.Video
import java.util.*
import java.util.concurrent.CompletableFuture

@Dao
interface VideoDao {
    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun getVideoById(videoid: UUID): CompletableFuture<Video?>
}
