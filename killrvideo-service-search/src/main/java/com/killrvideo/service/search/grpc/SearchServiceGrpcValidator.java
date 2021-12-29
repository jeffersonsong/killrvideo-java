package com.killrvideo.service.search.grpc;

import com.killrvideo.utils.FluentValidator;
import io.grpc.stub.StreamObserver;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class SearchServiceGrpcValidator  {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceGrpcValidator.class);

    public void validateGrpcRequest_GetQuerySuggestions(GetQuerySuggestionsRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getQuerySuggestions", request, LOGGER, streamObserver)
                .notEmpty("query string", isBlank(request.getQuery()))
                .positive("page size",request.getPageSize() <= 0)
                .validate();
    }
    
    /**
     * Validation for search.
     */
    public void validateGrpcRequest_SearchVideos(SearchVideosRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("searchVideos", request, LOGGER, streamObserver)
                .notEmpty("query string", isBlank(request.getQuery()))
                .positive("page size", request.getPageSize() <= 0)
                .validate();
    }
}
