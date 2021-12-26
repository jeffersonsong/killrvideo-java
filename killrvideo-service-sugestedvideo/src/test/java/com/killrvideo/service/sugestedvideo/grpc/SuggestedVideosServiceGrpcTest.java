package com.killrvideo.service.sugestedvideo.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.service.sugestedvideo.request.GetRelatedVideosRequestData;
import com.killrvideo.service.sugestedvideo.repository.SuggestedVideosRepository;
import io.grpc.stub.StreamObserver;
import killrvideo.suggested_videos.SuggestedVideosService.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class SuggestedVideosServiceGrpcTest {
    @InjectMocks private SuggestedVideosServiceGrpc service;
    @Mock
    private SuggestedVideosRepository suggestedVideosRepository;
    @Mock
    private SuggestedVideosServiceGrpcValidator validator;
    @Mock
    private SuggestedVideosServiceGrpcMapper mapper;

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
    public void testGetRelatedVideosWithValidationFailure() {
        GetRelatedVideosRequest grpcReq = GetRelatedVideosRequest.getDefaultInstance();
        StreamObserver<GetRelatedVideosResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(validator)
                .validateGrpcRequest_getRelatedVideo(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                this.service.getRelatedVideos(grpcReq, grpcResObserver)
        );
    }

    @Test
    public void testGetRelatedVideosWithQueryFailure() {
        GetRelatedVideosRequest grpcReq = getRelatedVideosRequest(UUID.randomUUID(), 5, "");
        StreamObserver<GetRelatedVideosResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequest_getRelatedVideo(any(), any());
        GetRelatedVideosRequestData requestData = mock(GetRelatedVideosRequestData.class);
        when(this.mapper.parseGetRelatedVideosRequestData(any())).thenReturn(requestData);

        when(this.suggestedVideosRepository.getRelatedVideos(any()))
                .thenReturn(CompletableFuture.failedFuture(new Exception()));

        this.service.getRelatedVideos(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    public void testGetRelatedVideos() {
        GetRelatedVideosRequest grpcReq = getRelatedVideosRequest(UUID.randomUUID(), 5, "");
        StreamObserver<GetRelatedVideosResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequest_getRelatedVideo(any(), any());
        GetRelatedVideosRequestData requestData = mock(GetRelatedVideosRequestData.class);
        when(this.mapper.parseGetRelatedVideosRequestData(any())).thenReturn(requestData);

        ResultListPage<Video> resultListPage = mock(ResultListPage.class);
        GetRelatedVideosResponse response = GetRelatedVideosResponse.getDefaultInstance();
        when(mapper.mapToGetRelatedVideosResponse(any(), any())).thenReturn(response);

        when(this.suggestedVideosRepository.getRelatedVideos(any()))
                .thenReturn(CompletableFuture.completedFuture(resultListPage));

        this.service.getRelatedVideos(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetSuggestedForUserWithValidationFailure() {
        GetSuggestedForUserRequest grpcReq = GetSuggestedForUserRequest.getDefaultInstance();
        StreamObserver<GetSuggestedForUserResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(validator)
                .validateGrpcRequest_getUserSuggestedVideo(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                this.service.getSuggestedForUser(grpcReq, grpcResObserver)
        );
    }

    private GetRelatedVideosRequest getRelatedVideosRequest(UUID videoid, int pageSize, String pagingState) {
        return GetRelatedVideosRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .setPageSize(pageSize)
                .setPagingState(pagingState)
                .build();
    }
}
