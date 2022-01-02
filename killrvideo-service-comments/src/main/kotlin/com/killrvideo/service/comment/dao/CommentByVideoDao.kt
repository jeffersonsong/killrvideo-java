package com.killrvideo.service.comment.dao

import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Insert
import com.datastax.oss.driver.api.mapper.annotations.Select
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes
import com.killrvideo.service.comment.dto.CommentByVideo
import java.util.*
import java.util.concurrent.CompletableFuture

@Dao
interface CommentByVideoDao {
    @Insert
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun insert(commentByVideo: CommentByVideo): CompletableFuture<Void>

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun find(videoid: UUID, commentid: UUID): CompletableFuture<CommentByVideo?>
}
