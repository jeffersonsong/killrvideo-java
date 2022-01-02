package com.killrvideo.service.statistic.grpc;

import io.grpc.stub.StreamObserver;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.killrvideo.utils.GrpcMappingUtils.randomUuid;
import static org.mockito.Mockito.*;

class StatisticsServiceGrpcValidatorTest {
    private final StatisticsServiceGrpcValidator validator = new StatisticsServiceGrpcValidator();
    private StreamObserver<?> streamObserver;

    @BeforeEach
    public void setUp() {
        this.streamObserver = mock(StreamObserver.class);
    }

    @Test
    public void testValidateGrpcRequest_GetNumberPlays_Success() {
        GetNumberOfPlaysRequest request = GetNumberOfPlaysRequest.newBuilder()
                .addVideoIds(randomUuid())
                .build();

        validator.validateGrpcRequest_GetNumberPlays(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_GetNumberPlays_Failure() {
        GetNumberOfPlaysRequest request = GetNumberOfPlaysRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_GetNumberPlays(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_RecordPlayback_Success() {
        RecordPlaybackStartedRequest request = RecordPlaybackStartedRequest.newBuilder()
                .setVideoId(randomUuid())
                .build();

        validator.validateGrpcRequest_RecordPlayback(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_RecordPlayback_Failure() {
        RecordPlaybackStartedRequest request = RecordPlaybackStartedRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_RecordPlayback(request, streamObserver)
        );

        verifyFailure();
    }

    private void verifySuccess() {
        verify(streamObserver, times(0)).onError(any());
        verify(streamObserver, times(0)).onCompleted();
    }

    private void verifyFailure() {
        verify(streamObserver, times(1)).onError(any());
        verify(streamObserver, times(1)).onCompleted();
    }
}