package com.killrvideo.service.statistic.dao;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;
import com.killrvideo.service.statistic.dto.VideoPlaybackStats;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Dao
public interface VideoPlaybackStatsDao {
    @Select(customWhereClause = "videoid in :listOfVideoIds")
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<MappedAsyncPagingIterable<VideoPlaybackStats>> getNumberOfPlays(@CqlName("listOfVideoIds") List<UUID> listOfVideoIds);

    @Increment(entityClass = VideoPlaybackStats.class)
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    CompletableFuture<Void> increment(UUID videoid, long views);
}
