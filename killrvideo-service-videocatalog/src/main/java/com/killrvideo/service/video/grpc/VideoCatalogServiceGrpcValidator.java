package com.killrvideo.service.video.grpc;

import static com.killrvideo.utils.ValidationUtils.initErrorString;
import static com.killrvideo.utils.ValidationUtils.validate;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest;

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
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (request.getVideoId() == null || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for submit youtube video request\n");
            isValid = false;
        }
        if (request.getUserId() == null || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for submit youtube video request\n");
            isValid = false;
        }
        if (isBlank(request.getName())) {
            errorMessage.append("\t\tvideo name should be provided for submit youtube video request\n");
            isValid = false;
        }
        if (isBlank(request.getDescription())) {
            errorMessage.append("\t\tvideo description should be provided for submit youtube video request\n");
            isValid = false;
        }
        if (isBlank(request.getYouTubeVideoId())) {
            errorMessage.append("\t\tvideo youtube id should be provided for submit youtube video request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'submitVideo'");
    }
    
    /**
     * Validate arguments for 'getLatestVideoPreview'
     */
    public void validateGrpcRequest_getLatestPreviews(GetLatestVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for get latest preview video request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid),  "Invalid parameter for 'getLatestVideoPreviews'");
    }
    
    public void validateGrpcRequest_getVideo(GetVideoRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (request.getVideoId() == null || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for submit youtube video request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid),  "Invalid parameter for 'getVideo'");
    }
    
    public void validateGrpcRequest_getVideoPreviews(GetVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getVideoIdsCount() >= 20) {
            errorMessage.append("\t\tcannot get more than 20 videos at once for get video previews request\n");
            isValid = false;
        }
        for (CommonTypes.Uuid uuid : request.getVideoIdsList()) {
            if (uuid == null || isBlank(uuid.getValue())) {
                errorMessage.append("\t\tprovided UUID values cannot be null or blank for get video previews request\n");
                isValid = false;
            }
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid),  "Invalid parameter for 'getVideoPreview'");
    }
    
    public void validateGrpcRequest_getUserVideoPreviews(GetUserVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getUserId() == null || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for get user video previews request\n");
            isValid = false;
        }

        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for get user video previews request\n");
            isValid = false;
        }        
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid),  "Invalid parameter for 'getUserVideoPreview'");
    }

}
