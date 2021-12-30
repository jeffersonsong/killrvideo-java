package com.killrvideo.service.sugestedvideo.grpc;

import io.grpc.stub.StreamObserver;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.killrvideo.utils.GrpcMappingUtils.randomUuid;
import static org.mockito.Mockito.*;

class SuggestedVideosServiceGrpcValidatorTest {
    private final SuggestedVideosServiceGrpcValidator validator = new SuggestedVideosServiceGrpcValidator();
    private StreamObserver<?> streamObserver;

    @BeforeEach
    public void setUp() {
        this.streamObserver = mock(StreamObserver.class);
    }

    @Test
    public void testValidateGrpcRequest_getRelatedVideo_Success() {
        GetRelatedVideosRequest request = GetRelatedVideosRequest.newBuilder()
                .setVideoId(randomUuid())
                .build();

        validator.validateGrpcRequest_getRelatedVideo(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_getRelatedVideo_Failure() {
        GetRelatedVideosRequest request = GetRelatedVideosRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_getRelatedVideo(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_getUserSuggestedVideo_Success() {
        GetSuggestedForUserRequest request = GetSuggestedForUserRequest.newBuilder()
                .setUserId(randomUuid())
                .build();

        validator.validateGrpcRequest_getUserSuggestedVideo(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_getUserSuggestedVideo_Failure() {
        GetSuggestedForUserRequest request = GetSuggestedForUserRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_getUserSuggestedVideo(request, streamObserver)
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