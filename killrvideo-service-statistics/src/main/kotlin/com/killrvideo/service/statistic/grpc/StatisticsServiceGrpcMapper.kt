package com.killrvideo.service.statistic.grpc;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.killrvideo.service.statistic.dto.VideoPlaybackStats;
import com.killrvideo.utils.GrpcMappingUtils;

import killrvideo.common.CommonTypes.Uuid;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse;
import killrvideo.statistics.StatisticsServiceOuterClass.PlayStats;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
public class StatisticsServiceGrpcMapper {
    
    public GetNumberOfPlaysResponse buildGetNumberOfPlayResponse(GetNumberOfPlaysRequest grpcReq, List<VideoPlaybackStats> videoList) {
        final Map<Uuid, PlayStats> result = videoList.stream()
                .filter(Objects::nonNull)
                .map(this::mapVideoPlayBacktoPlayStats)
                .collect(Collectors.toMap(PlayStats::getVideoId, x -> x));

        final GetNumberOfPlaysResponse.Builder builder = GetNumberOfPlaysResponse.newBuilder();
        for (Uuid requestedVideoId : grpcReq.getVideoIdsList()) {
            PlayStats playStats = result.computeIfAbsent(requestedVideoId, this::defaultPlayStats);
            builder.addStats(playStats);
        }
        return builder.build();
    }

    private PlayStats defaultPlayStats(Uuid requestedVideoId) {
        return PlayStats
                .newBuilder()
                .setVideoId(requestedVideoId)
                .setViews(0L)
                .build();
    }

    /**
     * Mapping to generated GPRC beans.
     */
    private PlayStats mapVideoPlayBacktoPlayStats(VideoPlaybackStats v) {
        return PlayStats.newBuilder()
                .setVideoId(uuidToUuid(v.getVideoid()))
                .setViews(Optional.ofNullable(v.getViews()).orElse(0L)).build();
    }

    public List<UUID> parseGetNumberOfPlaysRequest(GetNumberOfPlaysRequest grpcReq) {
        return grpcReq.getVideoIdsList()
                .stream()
                .filter(uuid -> isNotBlank(uuid.getValue()))
                .map(GrpcMappingUtils::fromUuid)
                .collect(Collectors.toList());
    }
}
