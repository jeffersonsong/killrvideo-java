package com.killrvideo.service.search.grpc;

import static com.killrvideo.utils.ValidationUtils.initErrorString;
import static com.killrvideo.utils.ValidationUtils.validate;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.grpc.stub.StreamObserver;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;

@Component
public class SearchServiceGrpcValidator  {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceGrpcValidator.class);

    public void validateGrpcRequest_GetQuerySuggestions(GetQuerySuggestionsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (isBlank(request.getQuery())) {
            errorMessage.append("\t\tquery string should be provided for get video suggestions request\n");
            isValid = false;
        }
        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for get video suggestions request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid),
                "Invalid parameter for 'getQuerySuggestions'");
    }
    
    /**
     * Validation for search.
     */
    public void validateGrpcRequest_SearchVideos(SearchVideosRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (isBlank(request.getQuery())) {
            errorMessage.append("\t\tquery string should be provided for search videos request\n");
            isValid = false;
        }
        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for search videos request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'searchVideos'");
    }
}
