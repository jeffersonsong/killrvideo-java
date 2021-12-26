package com.killrvideo.service.sugestedvideo.grpc;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.killrvideo.service.sugestedvideo.request.GetRelatedVideosRequestData;
import com.killrvideo.service.sugestedvideo.repository.SuggestedVideosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc.SuggestedVideoServiceImplBase;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse;
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse;

/**
 * Suggested video for a user.
 *
 * @author DataStax advocates Team
 */
@Service
public class SuggestedVideosServiceGrpc extends SuggestedVideoServiceImplBase {
    
    /** Loger for that class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestedVideosServiceGrpc.class);
     
    @Value("${killrvideo.discovery.services.suggestedVideo : SuggestedVideoService}")
    private String serviceKey;

    private final SuggestedVideosRepository suggestedVideosRepository;
    private final SuggestedVideosServiceGrpcValidator validator;
    private final SuggestedVideosServiceGrpcMapper mapper;

    public SuggestedVideosServiceGrpc(SuggestedVideosRepository suggestedVideosRepository, SuggestedVideosServiceGrpcValidator validator, SuggestedVideosServiceGrpcMapper mapper) {
        this.suggestedVideosRepository = suggestedVideosRepository;
        this.validator = validator;
        this.mapper = mapper;
    }

    /** {@inheritDoc} */
    @Override
    public void getRelatedVideos(GetRelatedVideosRequest grpcReq, StreamObserver<GetRelatedVideosResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_getRelatedVideo(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // Mapping GRPC => Domain (Dao)
        GetRelatedVideosRequestData requestData = mapper.parseGetRelatedVideosRequestData(grpcReq);

        // Invoke DAO Async
        suggestedVideosRepository.getRelatedVideos(requestData)
        .whenComplete((resultPage, error) -> {
            // Map Result back to GRPC
            if (error != null ) {
                traceError("getRelatedVideos", starts, error);
                grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                
            } else {
                traceSuccess( "getRelatedVideos", starts);
                grpcResObserver.onNext(mapper.mapToGetRelatedVideosResponse(resultPage, requestData.getVideoid()));
                grpcResObserver.onCompleted();
            }
        });
    }


    /** {@inheritDoc} */
    @Override
    public void getSuggestedForUser(GetSuggestedForUserRequest grpcReq, StreamObserver<GetSuggestedForUserResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_getUserSuggestedVideo(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // Mapping GRPC => Domain (Dao)
        final UUID userid = UUID.fromString(grpcReq.getUserId().getValue());
        
        // Invoke DAO Async
        suggestedVideosRepository.getSuggestedVideosForUser(userid)
        .whenComplete((videos, error) -> {
            // Map Result back to GRPC
            if (error != null ) {
                traceError("getSuggestedForUser", starts, error);
                grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                
            } else {
                traceSuccess("getSuggestedForUser", starts);
                Uuid userGrpcUUID = uuidToUuid(userid);
                final GetSuggestedForUserResponse.Builder builder = GetSuggestedForUserResponse.newBuilder().setUserId(userGrpcUUID);
                videos.stream().map(mapper::mapVideotoSuggestedVideoPreview).forEach(builder::addVideos);
                grpcResObserver.onNext(builder.build());
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
