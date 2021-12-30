package com.killrvideo.service.statistic.repository;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsDao;
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsMapper;
import com.killrvideo.service.statistic.dto.VideoPlaybackStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.killrvideo.utils.test.CassandraTestUtils.mockMappedAsyncPagingIterable;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsRepositoryTest {
    private StatisticsRepository repository;
    private VideoPlaybackStatsDao videoPlaybackStatsDao;

    @BeforeEach
    public void setUp() {
        VideoPlaybackStatsMapper mapper = mock(VideoPlaybackStatsMapper.class);
        this.videoPlaybackStatsDao = mock(VideoPlaybackStatsDao.class);
        when(mapper.getVideoPlaybackStatsDao()).thenReturn(videoPlaybackStatsDao);

        this.repository = new StatisticsRepository(mapper);
    }

    @Test
    public void testRecordPlaybackStartedAsync() {
        UUID videoid = UUID.randomUUID();
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(videoPlaybackStatsDao.increment(any(), anyLong())).thenReturn(future);

        CompletableFuture<Void> result = this.repository.recordPlaybackStartedAsync(videoid);
        assertEquals(future, result);
        verify(this.videoPlaybackStatsDao, times(1)).increment(any(), anyLong());
    }

    @Test
    public void testGetNumberOfPlaysAsync() {
        UUID videoid = UUID.randomUUID();
        VideoPlaybackStats videoPlaybackStats = mock(VideoPlaybackStats.class);
        List<UUID> videoids = singletonList(videoid);
        List<VideoPlaybackStats> videoPlaybackStatsList = singletonList(videoPlaybackStats);

        MappedAsyncPagingIterable<VideoPlaybackStats> iter = mockMappedAsyncPagingIterable(videoPlaybackStatsList);

        when(videoPlaybackStatsDao.getNumberOfPlays(any())).thenReturn(
            CompletableFuture.completedFuture(iter)
        );

        repository.getNumberOfPlaysAsync(videoids).whenComplete((result, error) -> {
            assertEquals(1, result.size());
            assertNull(error);
        });
    }
}
