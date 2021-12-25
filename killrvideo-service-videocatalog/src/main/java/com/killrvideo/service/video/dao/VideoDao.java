package com.killrvideo.service.video.dao;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;
import com.killrvideo.service.video.dto.Video;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Dao
public interface VideoDao {
    @Insert
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    CompletableFuture<Void> insert(Video video);

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<Video> getVideoById(UUID videoid);

    @Select(customWhereClause = "videoid in :listofVideoId")
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<MappedAsyncPagingIterable<Video>> getVideoPreview(@CqlName("listofVideoId") List<UUID> listofVideoId);
}
