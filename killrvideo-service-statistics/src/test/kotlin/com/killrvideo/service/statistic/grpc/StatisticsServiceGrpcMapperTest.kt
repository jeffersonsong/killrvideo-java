package com.killrvideo.service.statistic.grpc

import com.killrvideo.service.statistic.dto.VideoPlaybackStats
import com.killrvideo.service.statistic.grpc.StatisticsServiceGrpcMapper.GetNumberOfPlaysRequestExtensions.parse
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.statistics.getNumberOfPlaysRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

internal class StatisticsServiceGrpcMapperTest {
    private val mapper = StatisticsServiceGrpcMapper()
    @Test
    fun testBuildGetNumberOfPlayResponse() {
        val videoid1 = UUID.randomUUID()
        val videoid2 = UUID.randomUUID()
        val request = getNumberOfPlaysRequest {
            videoIds.add(uuidToUuid(videoid1))
            videoIds.add(uuidToUuid(videoid2))
        }
        val videoPlaybackStats1 = VideoPlaybackStats(videoid = videoid1, views = 2001L)
        val videoPlaybackStatsList = listOf(videoPlaybackStats1)
        val response = mapper.buildGetNumberOfPlayResponse(request, videoPlaybackStatsList)
        assertEquals(2, response.statsCount)
    }

    @Test
    fun testParseGetNumberOfPlaysRequest() {
        val videoid = UUID.randomUUID()
        val request = getNumberOfPlaysRequest {
            videoIds.add(uuidToUuid(videoid))
        }
        val result = request.parse()
        assertEquals(1, result.size)
        assertTrue(result.contains(videoid))
    }
}
