package com.killrvideo.service.statistic.repository

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsDao
import com.killrvideo.service.statistic.dao.VideoPlaybackStatsMapper
import com.killrvideo.service.statistic.dto.VideoPlaybackStats
import com.killrvideo.utils.test.CassandraTestUtils.mockMappedAsyncPagingIterable
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.CompletableFuture

internal class StatisticsRepositoryTest {
    private lateinit var repository: StatisticsRepository
    private lateinit var videoPlaybackStatsDao: VideoPlaybackStatsDao

    @BeforeEach
    fun setUp() {
        videoPlaybackStatsDao = mockk()
        val mapper = mockk<VideoPlaybackStatsMapper>()
        every { mapper.videoPlaybackStatsDao } returns videoPlaybackStatsDao
        repository = StatisticsRepository(mapper)
    }

    @Test
    fun testRecordPlaybackStartedAsync() {
        val videoid = UUID.randomUUID()
        val future = CompletableFuture.completedFuture<Void?>(null)
        every { videoPlaybackStatsDao.increment(any(), any()) } returns future
        runBlocking { repository.recordPlaybackStartedAsync(videoid) }
        verify(exactly = 1) {
            videoPlaybackStatsDao.increment(any(), any())
        }
    }

    @Test
    fun testGetNumberOfPlaysAsync() {
        val videoid = UUID.randomUUID()
        val videoPlaybackStats = mockk<VideoPlaybackStats>()

        val videoids: List<UUID> = listOf(videoid)
        val videoPlaybackStatsList = listOf(videoPlaybackStats)

        val iter: MappedAsyncPagingIterable<VideoPlaybackStats> =
            mockMappedAsyncPagingIterable(videoPlaybackStatsList)
        every { videoPlaybackStatsDao.getNumberOfPlays(any()) } returns
                CompletableFuture.completedFuture(iter)
        val result = runBlocking { repository.getNumberOfPlaysAsync(videoids) }
        assertEquals(1, result.size)
    }
}
