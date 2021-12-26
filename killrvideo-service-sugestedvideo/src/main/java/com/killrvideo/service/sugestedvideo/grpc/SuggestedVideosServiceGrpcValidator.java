package com.killrvideo.service.sugestedvideo.grpc;

import static com.killrvideo.utils.ValidationUtils.initErrorString;
import static com.killrvideo.utils.ValidationUtils.validate;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.grpc.stub.StreamObserver;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest;

@Component
public class SuggestedVideosServiceGrpcValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestedVideosServiceGrpcValidator.class);

    public void validateGrpcRequest_getRelatedVideo(GetRelatedVideosRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (isBlank(request.getVideoId().getValue())) {
             errorMessage.append("\t\tvideo id should be provided for get related videos request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'getRelatedVideo'");
    }
    
    
    public void validateGrpcRequest_getUserSuggestedVideo(GetSuggestedForUserRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for get suggested for user request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'getSuggestedForUser'");
    }
    
}
