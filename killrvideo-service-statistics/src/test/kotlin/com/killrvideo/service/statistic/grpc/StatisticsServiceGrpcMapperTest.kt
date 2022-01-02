package com.killrvideo.service.statistic.grpc;

import com.killrvideo.service.statistic.dto.VideoPlaybackStats;
import killrvideo.statistics.StatisticsServiceOuterClass.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class StatisticsServiceGrpcMapperTest {
    private final StatisticsServiceGrpcMapper mapper = new StatisticsServiceGrpcMapper();

    @Test
    public void testBuildGetNumberOfPlayResponse() {
        UUID videoid1 = UUID.randomUUID();
        UUID videoid2 = UUID.randomUUID();

        GetNumberOfPlaysRequest request = GetNumberOfPlaysRequest.newBuilder()
                .addVideoIds(uuidToUuid(videoid1))
                .addVideoIds(uuidToUuid(videoid2))
                .build();

        VideoPlaybackStats videoPlaybackStats1 = new VideoPlaybackStats(videoid1, 2001L);
        List<VideoPlaybackStats> videoPlaybackStatsList = singletonList(videoPlaybackStats1);

        GetNumberOfPlaysResponse response = mapper.buildGetNumberOfPlayResponse(request, videoPlaybackStatsList);
        assertEquals(2, response.getStatsCount());
    }

    @Test
    public void testParseGetNumberOfPlaysRequest() {
        UUID videio = UUID.randomUUID();
        GetNumberOfPlaysRequest request = GetNumberOfPlaysRequest.newBuilder()
                .addVideoIds(uuidToUuid(videio))
                .build();

        List<UUID> result = mapper.parseGetNumberOfPlaysRequest(request);
        assertEquals(1, result.size());
        assertTrue(result.contains(videio));
    }
}