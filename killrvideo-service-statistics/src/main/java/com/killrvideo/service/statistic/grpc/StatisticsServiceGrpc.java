package com.killrvideo.service.statistic.grpc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.killrvideo.service.statistic.repository.StatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.statistics.StatisticsServiceGrpc.StatisticsServiceImplBase;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse;

import javax.inject.Inject;

/**
 * Get statistics on a video.
 *
 * @author DataStax advocates Team
 */
@Service
public class StatisticsServiceGrpc extends StatisticsServiceImplBase {

    /** Loger for that class. */
    private static Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceGrpc.class);
    
    /** Stast services. */
    public static final String STATISTICS_SERVICE_NAME = "StatisticsService";
  
    @Value("${killrvideo.discovery.services.statistic : StatisticsService}")
    private String serviceKey;
    
    @Inject
    private StatisticsRepository statisticsRepository;
    @Inject
    private StatisticsServiceGrpcValidator validator;
    @Inject
    private StatisticsServiceGrpcMapper mapper;
    
    /** {@inheritDoc} */
    @Override
    public void recordPlaybackStarted(RecordPlaybackStartedRequest grpcReq, StreamObserver<RecordPlaybackStartedResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_RecordPlayback(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // Mapping GRPC => Domain (Dao)
        final UUID videoId = UUID.fromString(grpcReq.getVideoId().getValue());
        
        // Invoke DAO Async
        statisticsRepository.recordPlaybackStartedAsync(videoId).whenComplete((result, error) -> {
            // Map Result back to GRPC
            if (error != null ) {
                traceError("recordPlaybackStarted", starts, error);
                grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
            } else {
                grpcResObserver.onNext(RecordPlaybackStartedResponse.newBuilder().build());
                grpcResObserver.onCompleted();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void getNumberOfPlays(GetNumberOfPlaysRequest grpcReq, StreamObserver<GetNumberOfPlaysResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_GetNumberPlays(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // Mapping GRPC => Domain (Dao)
        List<UUID> listOfVideoId = grpcReq.getVideoIdsList()
                                           .stream()
                                           .map(Uuid::getValue)
                                           .map(UUID::fromString)
                                           .collect(Collectors.toList());
        
        // Invoke DAO Async
        statisticsRepository.getNumberOfPlaysAsync(listOfVideoId).whenComplete((videoList, error) -> {
            // Map Result back to GRPC
            if (error != null ) {
                traceError("getNumberOfPlays", starts, error);
                grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
            } else {
                
                traceSuccess("getNumberOfPlays", starts);
                grpcResObserver.onNext(mapper.buildGetNumberOfPlayResponse(grpcReq, videoList));
                grpcResObserver.onCompleted();
            }
        });
    }
    
    /**
     * Utility to TRACE.
     *
     * @param method
     *      current operation
     * @param starts
     *      timestamp for starting
     */
    private void traceSuccess(String method, Instant starts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("End successfully '{}' in {} millis", method, Duration.between(starts, Instant.now()).getNano()/1000);
        }
    }
    
    /**
     * Utility to TRACE.
     *
     * @param method
     *      current operation
     * @param starts
     *      timestamp for starting
     */
    private void traceError(String method, Instant starts, Throwable t) {
        LOGGER.error("An error occured in {} after {}", method, Duration.between(starts, Instant.now()), t);
    }

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return
     *       current value of 'serviceKey'
     */
    public String getServiceKey() {
        return serviceKey;
    }

    
}
