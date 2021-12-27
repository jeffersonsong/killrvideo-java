package com.killrvideo.service.statistic.grpc;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest;

import static com.killrvideo.utils.ValidationUtils.initErrorString;
import static com.killrvideo.utils.ValidationUtils.validate;

@Component
public class StatisticsServiceGrpcValidator {
    private static Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceGrpcValidator.class);
    
    public void validateGrpcRequest_GetNumberPlays(GetNumberOfPlaysRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (request.getVideoIdsCount() <= 0) {
            errorMessage.append("\t\tvideo ids should be provided for get number of plays request\n");
            isValid = false;
        }
        if (request.getVideoIdsCount() > 20) {
            errorMessage.append("\t\tcannot do a get more than 20 videos at once for get number of plays request\n");
            isValid = false;
        }
        for (CommonTypes.Uuid uuid : request.getVideoIdsList()) {
            if (uuid == null || isBlank(uuid.getValue())) {
                errorMessage.append("\t\tprovided UUID values cannot be null or blank for get number of plays request\n");
                isValid = false;
            }
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'getNumberPlays'");
    }
    
    public void validateGrpcRequest_RecordPlayback(RecordPlaybackStartedRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getVideoId() == null || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for record playback started request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'recordPlaybackStarted'");
    } 
}
