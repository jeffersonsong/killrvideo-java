package com.killrvideo.service.statistic.grpc;

import com.killrvideo.service.statistic.dto.VideoPlaybackStats;
import com.killrvideo.service.statistic.repository.StatisticsRepository;
import com.killrvideo.utils.GrpcMappingUtils;
import io.grpc.stub.StreamObserver;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StatisticsServiceGrpcTest {
    @InjectMocks
    StatisticsServiceGrpc service;

    @Mock
    private StatisticsRepository statisticsRepository;
    @Mock
    private StatisticsServiceGrpcValidator validator;
    @Mock
    private StatisticsServiceGrpcMapper mapper;

    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testRecordPlaybackStartedWithValidationFailure() {
        RecordPlaybackStartedRequest grpcReq = RecordPlaybackStartedRequest.getDefaultInstance();
        StreamObserver<RecordPlaybackStartedResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_RecordPlayback(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.recordPlaybackStarted(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testRecordPlaybackStartedWithQueryFailure() {
        RecordPlaybackStartedRequest grpcReq = recordPlaybackStartedRequest(UUID.randomUUID());
        StreamObserver<RecordPlaybackStartedResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_RecordPlayback(any(), any());

        when(this.statisticsRepository.recordPlaybackStartedAsync(any())).thenReturn(
                CompletableFuture.failedFuture(new Exception())
        );

        service.recordPlaybackStarted(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testRecordPlayback() {
        RecordPlaybackStartedRequest grpcReq = recordPlaybackStartedRequest(UUID.randomUUID());
        StreamObserver<RecordPlaybackStartedResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_RecordPlayback(any(), any());

        when(this.statisticsRepository.recordPlaybackStartedAsync(any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        service.recordPlaybackStarted(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetNumberOfPlaysWithValidationFailure() {
        GetNumberOfPlaysRequest grpcReq = GetNumberOfPlaysRequest.getDefaultInstance();
        StreamObserver<GetNumberOfPlaysResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_GetNumberPlays(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.getNumberOfPlays(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testGetNumberOfPlaysWithQueryFailure() {
        GetNumberOfPlaysRequest grpcReq = getNumberOfPlaysRequest(UUID.randomUUID());
        StreamObserver<GetNumberOfPlaysResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetNumberPlays(any(), any());
        when(this.statisticsRepository.getNumberOfPlaysAsync(any())).thenReturn(
                CompletableFuture.failedFuture(new Exception())
        );

        service.getNumberOfPlays(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetNumberOfPlays() {
        GetNumberOfPlaysRequest grpcReq = getNumberOfPlaysRequest(UUID.randomUUID());
        StreamObserver<GetNumberOfPlaysResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetNumberPlays(any(), any());

        List<VideoPlaybackStats> videoList = Collections.emptyList();
        GetNumberOfPlaysResponse response = GetNumberOfPlaysResponse.getDefaultInstance();
        when(mapper.buildGetNumberOfPlayResponse(any(), any())).thenReturn(response);

        when(this.statisticsRepository.getNumberOfPlaysAsync(any())).thenReturn(
                CompletableFuture.completedFuture(videoList)
        );

        service.getNumberOfPlays(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    private RecordPlaybackStartedRequest recordPlaybackStartedRequest(UUID videoid) {
        return RecordPlaybackStartedRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .build();
    }

    private GetNumberOfPlaysRequest getNumberOfPlaysRequest(UUID... videoids) {
        return GetNumberOfPlaysRequest.newBuilder()
                .addAllVideoIds(
                        Arrays.stream(videoids)
                                .map(GrpcMappingUtils::uuidToUuid)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
