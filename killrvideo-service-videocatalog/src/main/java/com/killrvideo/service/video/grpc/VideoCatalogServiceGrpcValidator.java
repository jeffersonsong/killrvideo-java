package com.killrvideo.service.video.grpc;

import com.killrvideo.utils.FluentValidator;
import io.grpc.stub.StreamObserver;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Validate arguments in GRPC services
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
public class VideoCatalogServiceGrpcValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoCatalogServiceGrpcValidator.class);

    /**
     * Validate arguments for 'SubmitYouTubeVideo'
     */
    public void validateGrpcRequest_submitYoutubeVideo(SubmitYouTubeVideoRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("submitVideo", request, LOGGER, streamObserver)
                .notEmpty("video id", isBlank(request.getVideoId().getValue()))
                .notEmpty("user id", isBlank(request.getUserId().getValue()))
                .notEmpty("video name", isBlank(request.getName()))
                .notEmpty("video description", isBlank(request.getDescription()))
                .notEmpty("video youtube id", isBlank(request.getYouTubeVideoId()))
                .validate();
    }

    /**
     * Validate arguments for 'getLatestVideoPreview'
     */
    public void validateGrpcRequest_getLatestPreviews(GetLatestVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getLatestVideoPreviews", request, LOGGER, streamObserver)
                .positive("page size", request.getPageSize() == 0)
                .validate();
    }

    public void validateGrpcRequest_getVideo(GetVideoRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getVideo", request, LOGGER, streamObserver)
                .notEmpty("video id", isBlank(request.getVideoId().getValue()))
                .validate();
    }

    public void validateGrpcRequest_getVideoPreviews(GetVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        FluentValidator validator = FluentValidator.of("getVideoPreview", request, LOGGER, streamObserver)
                .error("cannot get more than 20 videos at once for get video previews request",
                        request.getVideoIdsCount() >= 20);

        request.getVideoIdsList().forEach(uuid ->
                validator.error("provided UUID values cannot be null or blank for get video previews request",
                        uuid == null || isBlank(uuid.getValue()))
        );
        validator.validate();
    }

    public void validateGrpcRequest_getUserVideoPreviews(GetUserVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getUserVideoPreview", request, LOGGER, streamObserver)
                .notEmpty("user id", isBlank(request.getUserId().getValue()))
                .positive("page size", request.getPageSize() == 0)
                .validate();
    }

}
