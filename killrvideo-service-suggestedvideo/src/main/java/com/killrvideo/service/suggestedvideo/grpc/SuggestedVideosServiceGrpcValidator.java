package com.killrvideo.service.suggestedvideo.grpc;

import com.killrvideo.utils.FluentValidator;
import io.grpc.stub.StreamObserver;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class SuggestedVideosServiceGrpcValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestedVideosServiceGrpcValidator.class);

    public void validateGrpcRequest_getRelatedVideo(GetRelatedVideosRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getRelatedVideo", request, LOGGER, streamObserver)
                .notEmpty("video id", isBlank(request.getVideoId().getValue()))
                .validate();
    }
    
    
    public void validateGrpcRequest_getUserSuggestedVideo(GetSuggestedForUserRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getSuggestedForUser", request, LOGGER, streamObserver)
                .notEmpty("user id", isBlank(request.getUserId().getValue()))
                .validate();
    }
    
}
