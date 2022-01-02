package com.killrvideo.service.statistic.dao

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import com.datastax.oss.driver.api.mapper.annotations.*
import com.killrvideo.service.statistic.dto.VideoPlaybackStats
import java.util.*
import java.util.concurrent.CompletableFuture

@Dao
interface VideoPlaybackStatsDao {
    @Select(customWhereClause = "videoid in :listOfVideoIds")
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun getNumberOfPlays(@CqlName("listOfVideoIds") listOfVideoIds: List<UUID>):
            CompletableFuture<MappedAsyncPagingIterable<VideoPlaybackStats>>

    @Increment(entityClass = [VideoPlaybackStats::class])
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun increment(videoid: UUID, views: Long): CompletableFuture<Void>
}
