package com.killrvideo.service.rating.grpc;

import com.killrvideo.utils.FluentValidator;
import io.grpc.stub.StreamObserver;
import killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest;
import killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest;
import killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class RatingsServiceGrpcValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RatingsServiceGrpcValidator.class);

    public void validateGrpcRequest_RateVideo(RateVideoRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("rateVideo",  request, LOGGER, streamObserver)
                .notEmpty("video id",
                        !request.hasVideoId() || isBlank(request.getVideoId().getValue()))
                .notEmpty("user id",
                        !request.hasUserId() || isBlank(request.getUserId().getValue()))
                .validate();
    }

    public void validateGrpcRequest_GetRating(GetRatingRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getRating", request, LOGGER, streamObserver)
                .notEmpty("video id",
                        !request.hasVideoId() || isBlank(request.getVideoId().getValue()))
                .validate();
    }

    public void validateGrpcRequest_GetUserRating(GetUserRatingRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getUserRating", request, LOGGER, streamObserver)
            .notEmpty("video id",
                    !request.hasVideoId() || isBlank(request.getVideoId().getValue()))
                .notEmpty("user id",
                        !request.hasUserId() || isBlank(request.getUserId().getValue()))
                .validate();
    }
}
