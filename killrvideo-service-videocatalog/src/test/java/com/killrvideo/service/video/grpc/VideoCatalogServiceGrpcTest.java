package com.killrvideo.service.video.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.dto.UserVideo;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData;
import com.killrvideo.service.video.repository.VideoCatalogRepository;
import com.killrvideo.utils.GrpcMappingUtils;
import io.grpc.stub.StreamObserver;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import killrvideo.video_catalog.events.VideoCatalogEvents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class VideoCatalogServiceGrpcTest {
    @InjectMocks private VideoCatalogServiceGrpc service;
    @Mock
    private MessagingDao messagingDao;
    @Mock
    private VideoCatalogRepository videoCatalogRepository;
    @Mock
    private VideoCatalogServiceGrpcValidator validator;
    @Mock
    private VideoCatalogServiceGrpcMapper mapper;
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
    public void testSubmitYouTubeVideoWithValidationFailure() {
        SubmitYouTubeVideoRequest grpcReq = SubmitYouTubeVideoRequest.getDefaultInstance();
        StreamObserver<SubmitYouTubeVideoResponse > grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_submitYoutubeVideo(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.submitYouTubeVideo(grpcReq, grpcResObserver)
        );
    }

    @Test
    public void testSubmitYouTubeVideoWithInsertFailure() {
        SubmitYouTubeVideoRequest grpcReq = SubmitYouTubeVideoRequest.getDefaultInstance();
        StreamObserver<SubmitYouTubeVideoResponse > grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_submitYoutubeVideo(any(), any());
        Video video = mock(Video.class);
        when(this.mapper.mapSubmitYouTubeVideoRequestAsVideo(any())).thenReturn(video);

        when(this.videoCatalogRepository.insertVideoAsync(any())).thenReturn(
            CompletableFuture.failedFuture(new Exception())
        );

        service.submitYouTubeVideo(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    public void testSubmitYouTubeVideoWithSendFailure() {
        SubmitYouTubeVideoRequest grpcReq = SubmitYouTubeVideoRequest.getDefaultInstance();
        StreamObserver<SubmitYouTubeVideoResponse > grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_submitYoutubeVideo(any(), any());
        Video video = mock(Video.class);
        when(this.mapper.mapSubmitYouTubeVideoRequestAsVideo(any())).thenReturn(video);

        when(this.videoCatalogRepository.insertVideoAsync(any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );
        VideoCatalogEvents.YouTubeVideoAdded event = VideoCatalogEvents.YouTubeVideoAdded.getDefaultInstance();
        when(this.mapper.createYouTubeVideoAddedEvent(any())).thenReturn(event);
        when(this.messagingDao.sendEvent(any(), any())).thenReturn(
                CompletableFuture.failedFuture(new Exception())
        );

        service.submitYouTubeVideo(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    public void testSubmitYouTubeVideo() {
        SubmitYouTubeVideoRequest grpcReq = SubmitYouTubeVideoRequest.getDefaultInstance();
        StreamObserver<SubmitYouTubeVideoResponse > grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_submitYoutubeVideo(any(), any());
        Video video = mock(Video.class);
        when(this.mapper.mapSubmitYouTubeVideoRequestAsVideo(any())).thenReturn(video);

        when(this.videoCatalogRepository.insertVideoAsync(any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );
        VideoCatalogEvents.YouTubeVideoAdded event = VideoCatalogEvents.YouTubeVideoAdded.getDefaultInstance();
        when(this.mapper.createYouTubeVideoAddedEvent(any())).thenReturn(event);
        when(this.messagingDao.sendEvent(any(), any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        service.submitYouTubeVideo(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetLatestVideoPreviewsWithValidationFailure() {
        GetLatestVideoPreviewsRequest grpcReq = GetLatestVideoPreviewsRequest.getDefaultInstance();
        StreamObserver<GetLatestVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_getLatestPreviews(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.getLatestVideoPreviews(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testGetLatestVideoPreviewsWithQueryFailure() {
        GetLatestVideoPreviewsRequest grpcReq = GetLatestVideoPreviewsRequest.getDefaultInstance();
        StreamObserver<GetLatestVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getLatestPreviews(any(), any());

        GetLatestVideoPreviewsRequestData requestData = mock(GetLatestVideoPreviewsRequestData.class);
        when(this.mapper.parseGetLatestVideoPreviewsRequest(any(), any())).thenReturn(requestData);

        when(videoCatalogRepository.getLatestVideoPreviewsAsync(any())).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException())
        );

        service.getLatestVideoPreviews(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetLatestVideoPreviews() {
        GetLatestVideoPreviewsRequest grpcReq = GetLatestVideoPreviewsRequest.getDefaultInstance();
        StreamObserver<GetLatestVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getLatestPreviews(any(), any());

        GetLatestVideoPreviewsRequestData requestData = mock(GetLatestVideoPreviewsRequestData.class);
        when(this.mapper.parseGetLatestVideoPreviewsRequest(any(), any())).thenReturn(requestData);

        LatestVideosPage returnedPage = mock(LatestVideosPage.class);
        when(videoCatalogRepository.getLatestVideoPreviewsAsync(any())).thenReturn(
                CompletableFuture.completedFuture(returnedPage)
        );

        GetLatestVideoPreviewsResponse response = GetLatestVideoPreviewsResponse.getDefaultInstance();
        when(this.mapper.mapLatestVideoToGrpcResponse(any())).thenReturn(response);

        service.getLatestVideoPreviews(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetVideoWithValidationFailure() {
        GetVideoRequest grpcReq = GetVideoRequest.getDefaultInstance();
        StreamObserver<GetVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_getVideo(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.getVideo(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testGetVideoWithQueryFailure() {
        GetVideoRequest grpcReq = getVideoRequest(UUID.randomUUID());
        StreamObserver<GetVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getVideo(any(), any());
        when(videoCatalogRepository.getVideoById(any())).thenReturn(
                CompletableFuture.failedFuture(new Exception())
        );

        service.getVideo(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetVideoWithNoVideoFound() {
        GetVideoRequest grpcReq = getVideoRequest(UUID.randomUUID());
        StreamObserver<GetVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getVideo(any(), any());
        when(videoCatalogRepository.getVideoById(any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        service.getVideo(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetVideo() {
        GetVideoRequest grpcReq = getVideoRequest(UUID.randomUUID());
        StreamObserver<GetVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getVideo(any(), any());
        Video video = mock(Video.class);
        when(videoCatalogRepository.getVideoById(any())).thenReturn(
                CompletableFuture.completedFuture(video)
        );
        GetVideoResponse response = GetVideoResponse.getDefaultInstance();
        when(this.mapper.mapFromVideotoVideoResponse(any())).thenReturn(response);

        service.getVideo(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetVideoPreviewsWithValidationFailure() {
        GetVideoPreviewsRequest grpcReq = GetVideoPreviewsRequest.getDefaultInstance();
        StreamObserver<GetVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_getVideoPreviews(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.getVideoPreviews(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testGetVideoPreviewsWithoutVideioId() {
        GetVideoPreviewsRequest grpcReq = getVideoPreviewsRequest();
        StreamObserver<GetVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getVideoPreviews(any(), any());
        service.getVideoPreviews(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetVideoPreviewsWithQueryFailure() {
        UUID videoid = UUID.randomUUID();
        GetVideoPreviewsRequest grpcReq = getVideoPreviewsRequest(videoid);
        StreamObserver<GetVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getVideoPreviews(any(), any());
        when(this.videoCatalogRepository.getVideoPreview(any())).thenReturn(
            CompletableFuture.failedFuture(new Exception())
        );
        service.getVideoPreviews(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetVideoPreviews() {
        UUID videoid = UUID.randomUUID();
        GetVideoPreviewsRequest grpcReq = getVideoPreviewsRequest(videoid);
        StreamObserver<GetVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getVideoPreviews(any(), any());
        Video video = mock(Video.class);
        List<Video> videos = singletonList(video);
        when(this.videoCatalogRepository.getVideoPreview(any())).thenReturn(
                CompletableFuture.completedFuture(videos)
        );

        GetVideoPreviewsResponse response = GetVideoPreviewsResponse.getDefaultInstance();
        when(this.mapper.mapToGetVideoPreviewsResponse(any())).thenReturn(response);

        service.getVideoPreviews(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetUserVideoPreviewsWithValidationFailure() {
        GetUserVideoPreviewsRequest grpcReq = GetUserVideoPreviewsRequest.getDefaultInstance();
        StreamObserver<GetUserVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_getUserVideoPreviews(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.getUserVideoPreviews(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testGetUserVideoPreviewsWithQueryFailure() {
        UUID userid = UUID.randomUUID();
        GetUserVideoPreviewsRequest grpcReq = getUserVideoPreviewsRequest(userid);
        StreamObserver<GetUserVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getUserVideoPreviews(any(), any());

        GetUserVideoPreviewsRequestData requestData = getUserVideoPreviewsRequestData(userid);
        when(this.mapper.parseGetUserVideoPreviewsRequest(grpcReq)).thenReturn(requestData);

        when(this.videoCatalogRepository.getUserVideosPreview(any())).thenReturn(
                CompletableFuture.failedFuture(new Exception())
        );

        service.getUserVideoPreviews(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetUserVideoPreviews() {
        UUID userid = UUID.randomUUID();
        GetUserVideoPreviewsRequest grpcReq = getUserVideoPreviewsRequest(userid);
        StreamObserver<GetUserVideoPreviewsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getUserVideoPreviews(any(), any());

        GetUserVideoPreviewsRequestData requestData = getUserVideoPreviewsRequestData(userid);
        when(this.mapper.parseGetUserVideoPreviewsRequest(grpcReq)).thenReturn(requestData);

        ResultListPage<UserVideo> resultListPage = mock(ResultListPage.class);

        when(this.videoCatalogRepository.getUserVideosPreview(any())).thenReturn(
                CompletableFuture.completedFuture(resultListPage)
        );
        GetUserVideoPreviewsResponse response = GetUserVideoPreviewsResponse.getDefaultInstance();
        when(this.mapper.mapToGetUserVideoPreviewsResponse(any(), any())).thenReturn(response);

        service.getUserVideoPreviews(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    private GetVideoRequest getVideoRequest(UUID videoid) {
        return GetVideoRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .build();
    }

    private GetVideoPreviewsRequest getVideoPreviewsRequest(UUID... videoIds) {
        return GetVideoPreviewsRequest.newBuilder()
                .addAllVideoIds(
                        Arrays.stream(videoIds).map(GrpcMappingUtils::uuidToUuid).collect(Collectors.toList())
                )
                .build();
    }

    private GetUserVideoPreviewsRequest getUserVideoPreviewsRequest(UUID userid) {
        return GetUserVideoPreviewsRequest.newBuilder()
                .setUserId(uuidToUuid(userid))
                .build();
    }

    private GetUserVideoPreviewsRequestData getUserVideoPreviewsRequestData(UUID userid) {
        return new GetUserVideoPreviewsRequestData(userid);
    }
}