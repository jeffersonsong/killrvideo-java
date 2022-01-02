package com.killrvideo.service.video.dao

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import com.datastax.oss.driver.api.mapper.annotations.*
import com.killrvideo.service.video.dto.Video
import java.util.*
import java.util.concurrent.CompletableFuture

@Dao
interface VideoDao {
    @Insert
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun insert(video: Video): CompletableFuture<Void>

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun getVideoById(videoid: UUID): CompletableFuture<Video?>

    @Select(customWhereClause = "videoid in :listofVideoId")
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun getVideoPreview(@CqlName("listofVideoId") listofVideoId: List<UUID>):
            CompletableFuture<MappedAsyncPagingIterable<Video>>
}
