package com.killrvideo.service.rating.grpc;

import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.rating.dto.VideoRatingByUser;
import com.killrvideo.service.rating.repository.RatingRepository;
import com.killrvideo.service.rating.request.GetUserRatingRequestData;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.ratings.RatingsServiceGrpc.RatingsServiceImplBase;
import killrvideo.ratings.RatingsServiceOuterClass.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static com.killrvideo.utils.GrpcUtils.returnSingleResult;

/**
 * Operations on Ratings with GRPC.
 *
 * @author DataStax advocates Team
 */
@Service
public class RatingsServiceGrpc extends RatingsServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RatingsServiceGrpc.class);

    @Value("${killrvideo.discovery.services.rating : RatingsService}")
    private String serviceKey;

    @Value("${killrvideo.messaging.kafka.topics.videoRated : topic-kv-videoRating}")
    private String topicvideoRated;

    private final RatingRepository ratingRepository;
    /**
     * Inter-service communications (messaging).
     */
    private final MessagingDao messagingDao;
    private final RatingsServiceGrpcValidator validator;
    private final RatingsServiceGrpcMapper mapper;

    public RatingsServiceGrpc(RatingRepository ratingRepository, MessagingDao messagingDao, RatingsServiceGrpcValidator validator, RatingsServiceGrpcMapper mapper) {
        this.ratingRepository = ratingRepository;
        this.messagingDao = messagingDao;
        this.validator = validator;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rateVideo(final RateVideoRequest grpcReq, final StreamObserver<RateVideoResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_RateVideo(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao)
        VideoRatingByUser videoRatingByUser = mapper.parseRateVideoRequest(grpcReq);

        // Invoking Dao (Async), publish event if successful
        ratingRepository.rateVideo(videoRatingByUser)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        traceError("rateVideo", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());

                    } else {
                        traceSuccess("rateVideo", starts);
                        messagingDao.sendEvent(topicvideoRated, mapper.createUserRatedVideoEvent(videoRatingByUser));
                        returnSingleResult(RateVideoResponse.newBuilder().build(), grpcResObserver);
                    }
                });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void getRating(GetRatingRequest grpcReq, StreamObserver<GetRatingResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_GetRating(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao)
        UUID videoid = UUID.fromString(grpcReq.getVideoId().getValue());

        // Invoking Dao (Async) and map result back to GRPC (maptoRatingResponse)
        ratingRepository.findRating(videoid)
                .whenComplete((videoRating, error) -> {
                    if (error != null) {
                        traceError("getRating", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());

                    } else {
                        traceSuccess("getRating", starts);
                        GetRatingResponse response =
                                videoRating.map(mapper::mapToRatingResponse)
                                        .orElse(emptyRatingResponse(videoid));
                        returnSingleResult(response, grpcResObserver);
                    }
                });
    }

    private GetRatingResponse emptyRatingResponse(UUID videoid) {
        return GetRatingResponse.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .setRatingsCount(0L)
                .setRatingsTotal(0L)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getUserRating(GetUserRatingRequest grpcReq, StreamObserver<GetUserRatingResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_GetUserRating(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao)
        GetUserRatingRequestData requestData = mapper.parseGetUserRatingRequest(grpcReq);

        // Invoking Dao (Async) and map result back to GRPC (maptoRatingResponse)
        ratingRepository.findUserRating(requestData).whenComplete((videoRating, error) -> {
            if (error != null) {
                traceError("getUserRating", starts, error);
                grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());

            } else {
                traceSuccess("getUserRating", starts);

                GetUserRatingResponse response =
                        videoRating.map(mapper::mapToUserRatingResponse)
                                .orElse(emptyUserRatingResponse(requestData));
                returnSingleResult(response, grpcResObserver);
            }
        });
    }

    private GetUserRatingResponse emptyUserRatingResponse(GetUserRatingRequestData request) {
        return GetUserRatingResponse.newBuilder()
                .setUserId(uuidToUuid(request.getUserid()))
                .setVideoId(uuidToUuid(request.getVideoid()))
                .setRating(0)
                .build();
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    private void traceSuccess(String method, Instant starts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("End successfully '{}' in {} millis", method, Duration.between(starts, Instant.now()).getNano() / 1000);
        }
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    private void traceError(String method, Instant starts, Throwable t) {
        LOGGER.error("An error occured in {} after {}", method, Duration.between(starts, Instant.now()), t);
    }

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return current value of 'serviceKey'
     */
    public String getServiceKey() {
        return serviceKey;
    }
}
