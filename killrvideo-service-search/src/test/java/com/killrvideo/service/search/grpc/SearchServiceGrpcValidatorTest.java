package com.killrvideo.service.search.grpc;

import io.grpc.stub.StreamObserver;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchServiceGrpcValidatorTest {
    private SearchServiceGrpcValidator validator = new SearchServiceGrpcValidator();
    private StreamObserver<?> streamObserver;

    @BeforeEach
    public void setUp() {
        this.streamObserver = mock(StreamObserver.class);
    }

    @Test
    public void testValidateGrpcRequest_GetQuerySuggestions_Success() {
        GetQuerySuggestionsRequest request = GetQuerySuggestionsRequest.newBuilder()
                .setQuery("Query")
                .setPageSize(2)
                .build();

        validator.validateGrpcRequest_GetQuerySuggestions(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_GetQuerySuggestions_Failure() {
        GetQuerySuggestionsRequest request = GetQuerySuggestionsRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_GetQuerySuggestions(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_SearchVideos_Success() {
        SearchVideosRequest request = SearchVideosRequest.newBuilder()
                .setQuery("Query")
                .setPageSize(2)
                .build();

        validator.validateGrpcRequest_SearchVideos(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_SearchVideos_Failure() {
        SearchVideosRequest request = SearchVideosRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_SearchVideos(request, streamObserver)
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