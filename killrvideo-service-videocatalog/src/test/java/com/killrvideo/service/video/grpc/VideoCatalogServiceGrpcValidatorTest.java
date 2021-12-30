package com.killrvideo.service.video.grpc;

import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.killrvideo.utils.GrpcMappingUtils.randomUuid;
import static org.mockito.Mockito.*;

class VideoCatalogServiceGrpcValidatorTest {
    private final VideoCatalogServiceGrpcValidator validator = new VideoCatalogServiceGrpcValidator();
    private StreamObserver<?> streamObserver;

    @BeforeEach
    public void setUp() {
        this.streamObserver = mock(StreamObserver.class);
    }

    @Test
    public void testValidateGrpcRequest_submitYoutubeVideo_Success() {
        SubmitYouTubeVideoRequest request = SubmitYouTubeVideoRequest.newBuilder()
                .setVideoId(randomUuid())
                .setUserId(randomUuid())
                .setName("Game")
                .setDescription("Game")
                .setYouTubeVideoId("xyz123")
                .build();
        validator.validateGrpcRequest_submitYoutubeVideo(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_submitYoutubeVideo_Failure() {
        SubmitYouTubeVideoRequest request = SubmitYouTubeVideoRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_submitYoutubeVideo(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_getLatestPreviews_Success() {
        GetLatestVideoPreviewsRequest request = GetLatestVideoPreviewsRequest.newBuilder()
                .setPageSize(2)
                .build();
        validator.validateGrpcRequest_getLatestPreviews(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_getLatestPreviews_Failure() {
        GetLatestVideoPreviewsRequest request = GetLatestVideoPreviewsRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_getLatestPreviews(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_getVideo_Success() {
        GetVideoRequest request = GetVideoRequest.newBuilder()
                .setVideoId(randomUuid())
                .build();
        validator.validateGrpcRequest_getVideo(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_getVideo_Failure() {
        GetVideoRequest request = GetVideoRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_getVideo(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_getVideoPreviews_Success() {
        GetVideoPreviewsRequest request = GetVideoPreviewsRequest.newBuilder()
                .addVideoIds(randomUuid())
                .build();

        validator.validateGrpcRequest_getVideoPreviews(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_getVideoPreviews_Failure() {
        List<CommonTypes.Uuid> videoIds = IntStream.range(0, 21)
                .mapToObj(i -> randomUuid()).collect(Collectors.toList());

        GetVideoPreviewsRequest request = GetVideoPreviewsRequest.newBuilder()
                .addAllVideoIds(videoIds)
                .build();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_getVideoPreviews(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_getUserVideoPreviews_Success() {
        GetUserVideoPreviewsRequest request = GetUserVideoPreviewsRequest.newBuilder()
                .setUserId(randomUuid())
                .setPageSize(2)
                .build();
        validator.validateGrpcRequest_getUserVideoPreviews(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_getUserVideoPreviews_Failure() {
        GetUserVideoPreviewsRequest request = GetUserVideoPreviewsRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_getUserVideoPreviews(request, streamObserver)
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