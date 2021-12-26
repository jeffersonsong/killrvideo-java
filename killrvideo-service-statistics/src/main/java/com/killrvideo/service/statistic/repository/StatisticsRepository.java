package com.killrvideo.service.statistic.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsDao;
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsMapper;
import com.killrvideo.service.statistic.dto.VideoPlaybackStats;
import com.killrvideo.dse.utils.MappedAsyncPagingIterableUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Implementations of operation for Videos.
 *
 * @author Jefferson Song
 */
@Repository
public class StatisticsRepository {
    private CqlSession session;
    private VideoPlaybackStatsDao videoPlaybackStatsDao;

    private PreparedStatement incrRecordPlayBacks;

    public StatisticsRepository(CqlSession session) {
        this.session = session;
        VideoPlaybackStatsMapper mapper = VideoPlaybackStatsMapper.build(session).build();
        this.videoPlaybackStatsDao = mapper.getVideoPlaybackStatsDao();
        SimpleStatement queryIncPlayBack = QueryBuilder.update("video_playback_stats")
                .increment(VideoPlaybackStats.COLUMN_VIEWS)
                .whereColumn(VideoPlaybackStats.COLUMN_VIDEOID).isEqualTo(QueryBuilder.bindMarker())
                .build();
        queryIncPlayBack.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        incrRecordPlayBacks = session.prepare(queryIncPlayBack);
    }

    /**
     * Increment counter in DB (Async).
     *
     * @param videoId
     *      current videoid.
     */
    public CompletableFuture<Void> recordPlaybackStartedAsync(UUID videoId) {
        Assert.notNull(videoId, "videoid is required to update statistics");
        BoundStatement statement = incrRecordPlayBacks.bind()
                .setUuid(VideoPlaybackStats.COLUMN_VIDEOID, videoId);
        CompletionStage<AsyncResultSet> future1 = session.executeAsync(statement);
        return future1.toCompletableFuture().thenApply(s -> null);
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
                .thenApply(MappedAsyncPagingIterableUtils::toList);
    }
}
