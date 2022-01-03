package com.killrvideo.service.statistic.grpc

import com.killrvideo.service.statistic.dto.VideoPlaybackStats
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.common.CommonTypes
import killrvideo.statistics.StatisticsServiceOuterClass.*
import killrvideo.statistics.getNumberOfPlaysResponse
import killrvideo.statistics.playStats
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
class StatisticsServiceGrpcMapper {

    object GetNumberOfPlaysRequestExtensions {
        fun GetNumberOfPlaysRequest.parse(): List<UUID> =
            this.videoIdsList.stream()
                .filter { isNotBlank(it.value) }
                .map { fromUuid(it) }
                .collect(Collectors.toList())
    }

    fun buildGetNumberOfPlayResponse(
        grpcReq: GetNumberOfPlaysRequest,
        videoList: List<VideoPlaybackStats>
    ): GetNumberOfPlaysResponse {
        val result = videoList.stream()
            .map { mapVideoPlayBacktoPlayStats(it) }
            .collect(Collectors.toMap(
                { obj: PlayStats -> obj.videoId }, { x: PlayStats -> x })
            )
        return getNumberOfPlaysResponse {
            for (requestedVideoId in grpcReq.videoIdsList) {
                val playStats = result.computeIfAbsent(requestedVideoId) {
                    defaultPlayStats(it)
                }

                stats.add(playStats)
            }
        }
    }

    private fun defaultPlayStats(requestedVideoId: CommonTypes.Uuid): PlayStats =
        playStats {
            videoId = requestedVideoId
            views = 0L
        }

    /**
     * Mapping to generated GPRC beans.
     */
    private fun mapVideoPlayBacktoPlayStats(v: VideoPlaybackStats): PlayStats =
        playStats {
            videoId = uuidToUuid(v.videoid!!)
            views = v.views ?: 0L
        }
}
