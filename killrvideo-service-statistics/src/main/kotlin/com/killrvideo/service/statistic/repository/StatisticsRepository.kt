package com.killrvideo.service.statistic.repository

import com.killrvideo.dse.utils.MappedAsyncPagingIterableExtensions.all
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsDao
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsMapper
import com.killrvideo.service.statistic.dto.VideoPlaybackStats
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
class StatisticsRepository(mapper: VideoPlaybackStatsMapper) {
    private val videoPlaybackStatsDao: VideoPlaybackStatsDao = mapper.videoPlaybackStatsDao

    /**
     * Increment counter in DB (Async).
     *
     * @param videoId
     * current videoid.
     */
    suspend fun recordPlaybackStartedAsync(videoId: UUID): Int =
        videoPlaybackStatsDao.increment(videoId, 1L)
            .thenApply { 1 }
            .await()

    /**
     * Search for each videoid.
     *
     * @param listOfVideoIds
     * list of EXISTING videoid
     * @return
     * future for the list
     */
    suspend fun getNumberOfPlaysAsync(listOfVideoIds: List<UUID>): List<VideoPlaybackStats> =
        videoPlaybackStatsDao.getNumberOfPlays(listOfVideoIds)
            .thenApply { it.all() }
            .await()
}
