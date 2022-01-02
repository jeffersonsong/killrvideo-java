package com.killrvideo.service.comment.dao

import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Insert
import com.datastax.oss.driver.api.mapper.annotations.Select
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes
import com.killrvideo.service.comment.dto.CommentByUser
import java.util.*
import java.util.concurrent.CompletableFuture

@Dao
interface CommentByUserDao {
    @Insert
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun insert(commentByUser: CommentByUser): CompletableFuture<Void>

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun find(userid: UUID, commentid: UUID): CompletableFuture<CommentByUser?>
}