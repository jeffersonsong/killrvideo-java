package com.killrvideo.service.statistic.grpc;

import com.killrvideo.utils.FluentValidator;
import io.grpc.stub.StreamObserver;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class StatisticsServiceGrpcValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceGrpcValidator.class);
    
    public void validateGrpcRequest_GetNumberPlays(GetNumberOfPlaysRequest request, StreamObserver<?> streamObserver) {
        FluentValidator validator = FluentValidator.of("getNumberPlays", request, LOGGER, streamObserver)
                .notEmpty("video ids", request.getVideoIdsCount() == 0)
                .error("cannot do a get more than 20 videos at once for get number of plays request",
                        request.getVideoIdsCount() > 20);

        request.getVideoIdsList().forEach(uuid ->
                validator.error("provided UUID values cannot be null or blank for get number of plays request",
                        uuid == null || isBlank(uuid.getValue()))
                );

        validator.validate();
    }
    
    public void validateGrpcRequest_RecordPlayback(RecordPlaybackStartedRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("recordPlaybackStarted", request, LOGGER, streamObserver)
                .notEmpty("video id", isBlank(request.getVideoId().getValue()))
                .validate();
    } 
}
