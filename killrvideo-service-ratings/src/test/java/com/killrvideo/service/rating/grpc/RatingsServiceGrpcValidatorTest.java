package com.killrvideo.service.rating.grpc;

import io.grpc.stub.StreamObserver;
import killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest;
import killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest;
import killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.killrvideo.utils.GrpcMappingUtils.randomUuid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RatingsServiceGrpcValidatorTest {
    private RatingsServiceGrpcValidator validator = new RatingsServiceGrpcValidator();
    private StreamObserver<?> streamObserver;

    @BeforeEach
    public void setUp() {
        this.streamObserver = mock(StreamObserver.class);
    }

    @Test
    public void testValidateGrpcRequest_RateVideo_Success() {
        RateVideoRequest request = RateVideoRequest.newBuilder()
                .setVideoId(randomUuid())
                .setUserId(randomUuid())
                .build();

        validator.validateGrpcRequest_RateVideo(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_RateVideo_Failure() {
        RateVideoRequest request = RateVideoRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_RateVideo(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_GetRating_Success() {
        GetRatingRequest request = GetRatingRequest.newBuilder()
                .setVideoId(randomUuid())
                .build();

        validator.validateGrpcRequest_GetRating(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_GetRating_Failure() {
        GetRatingRequest request = GetRatingRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_GetRating(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_GetUserRating_Success() {
        GetUserRatingRequest request = GetUserRatingRequest.newBuilder()
                .setVideoId(randomUuid())
                .setUserId(randomUuid())
                .build();

        validator.validateGrpcRequest_GetUserRating(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_GetUserRating_Failure() {
        GetUserRatingRequest request = GetUserRatingRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_GetUserRating(request, streamObserver)
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