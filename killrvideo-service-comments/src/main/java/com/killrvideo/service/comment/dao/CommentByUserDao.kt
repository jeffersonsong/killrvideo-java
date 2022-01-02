package com.killrvideo.service.comment.dao;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes;
import com.killrvideo.service.comment.dto.CommentByUser;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Dao
public interface CommentByUserDao {
    @Insert
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    CompletableFuture<Void> insert(CommentByUser commentByUser);

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<CommentByUser> find(UUID userid, UUID commentid);
}
