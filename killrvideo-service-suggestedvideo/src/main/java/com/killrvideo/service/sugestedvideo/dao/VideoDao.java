package com.killrvideo.service.sugestedvideo.dao;

import com.datastax.oss.driver.api.mapper.annotations.*;
import com.killrvideo.dse.dto.Video;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Dao
public interface VideoDao {
    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<Video> getVideoById(UUID videoid);
}
