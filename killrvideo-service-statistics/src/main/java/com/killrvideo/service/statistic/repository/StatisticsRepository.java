package com.killrvideo.service.statistic.repository;

import com.killrvideo.service.statistic.dao.VideoPlaybackStatsDao;
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsMapper;
import com.killrvideo.service.statistic.dto.VideoPlaybackStats;
import com.killrvideo.dse.utils.MappedAsyncPagingIterableUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
public class StatisticsRepository {
    private final VideoPlaybackStatsDao videoPlaybackStatsDao;

    public StatisticsRepository(VideoPlaybackStatsMapper mapper) {
        this.videoPlaybackStatsDao = mapper.getVideoPlaybackStatsDao();
    }

    /**
     * Increment counter in DB (Async).
     *
     * @param videoId
     *      current videoid.
     */
    public CompletableFuture<Void> recordPlaybackStartedAsync(UUID videoId) {
        Assert.notNull(videoId, "videoid is required to update statistics");
        return videoPlaybackStatsDao.increment(videoId, 1L);
    }

    /**
     * Search for each videoid.
     *
     * @param listOfVideoIds
     *      list of EXISTING videoid
     * @return
     *      future for the list
     */
    public CompletableFuture<List<VideoPlaybackStats>> getNumberOfPlaysAsync(List<UUID> listOfVideoIds) {
        Assert.notNull(listOfVideoIds, "videoid list cannot be null");
        return videoPlaybackStatsDao.getNumberOfPlays(listOfVideoIds)
                .thenApply(MappedAsyncPagingIterableUtils::all);
    }
}
