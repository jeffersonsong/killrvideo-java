package com.killrvideo.service.search.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.service.search.repository.SearchRepository;
import io.grpc.stub.StreamObserver;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchServiceGrpcTest {
    @InjectMocks private SearchServiceGrpc service;
    @Mock
    private SearchRepository searchRepository;
    @Mock
    private SearchServiceGrpcValidator validator;
    @Mock
    private SearchServiceGrpcMapper mapper;

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
    void testSearchVideosWithValidationFailed() {
        SearchVideosRequest grpcReq = SearchVideosRequest.getDefaultInstance();
        StreamObserver<SearchVideosResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_SearchVideos(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                this.service.searchVideos(grpcReq, grpcResObserver));
    }

    @Test
    void testSearchVideosWithQueryFailed() {
        SearchVideosRequest grpcReq = SearchVideosRequest.getDefaultInstance();
        StreamObserver<SearchVideosResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_SearchVideos(any(), any());

        when(searchRepository.searchVideosAsync(any(), anyInt(), any()))
                .thenReturn(CompletableFuture.failedFuture(new Exception()));

        this.service.searchVideos(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testSearchVideos() {
        SearchVideosRequest grpcReq = SearchVideosRequest.getDefaultInstance();
        StreamObserver<SearchVideosResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_SearchVideos(any(), any());

        ResultListPage<Video> resultPage = mock(ResultListPage.class);
        SearchVideosResponse response = SearchVideosResponse.getDefaultInstance();
        when(mapper.buildSearchGrpcResponse(any(), any())).thenReturn(response);

        when(searchRepository.searchVideosAsync(any(), anyInt(), any()))
                .thenReturn(CompletableFuture.completedFuture(resultPage));

        this.service.searchVideos(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetQuerySuggestionsWithValidationFailed() {
        GetQuerySuggestionsRequest grpcReq = GetQuerySuggestionsRequest.getDefaultInstance();
        StreamObserver<GetQuerySuggestionsResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_GetQuerySuggestions(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                this.service.getQuerySuggestions(grpcReq, grpcResObserver));
    }

    @Test
    void testGetQuerySuggestionsWithQueryFailed() {
        GetQuerySuggestionsRequest grpcReq = GetQuerySuggestionsRequest.getDefaultInstance();
        StreamObserver<GetQuerySuggestionsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetQuerySuggestions(any(), any());
        when(this.searchRepository.getQuerySuggestionsAsync(any(), anyInt())).thenReturn(
                CompletableFuture.failedFuture(new Exception())
        );

        this.service.getQuerySuggestions(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetQuerySuggestions() {
        GetQuerySuggestionsRequest grpcReq = GetQuerySuggestionsRequest.getDefaultInstance();
        StreamObserver<GetQuerySuggestionsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_GetQuerySuggestions(any(), any());
        Set<String> suggestionSet = Collections.singleton("Test");
        GetQuerySuggestionsResponse response = GetQuerySuggestionsResponse.getDefaultInstance();
        when(mapper.buildQuerySuggestionsResponse(any(), any())).thenReturn(response);

        when(this.searchRepository.getQuerySuggestionsAsync(any(), anyInt())).thenReturn(
                CompletableFuture.completedFuture(suggestionSet)
        );

        this.service.getQuerySuggestions(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    private SearchVideosRequest searchVideosRequest(String query, int pageSize, String pageState) {
        return SearchVideosRequest.newBuilder()
                .setQuery(query)
                .setPageSize(pageSize)
                .setPagingState(pageState)
                .build();
    }

    private GetQuerySuggestionsRequest getQuerySuggestionsRequest(String query, int pageSize) {
        return GetQuerySuggestionsRequest.newBuilder()
                .setQuery(query)
                .setPageSize(pageSize)
                .build();
    }
}