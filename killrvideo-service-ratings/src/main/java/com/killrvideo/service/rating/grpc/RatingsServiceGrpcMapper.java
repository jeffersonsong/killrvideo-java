package com.killrvideo.service.rating.grpc;

import static com.killrvideo.utils.GrpcMappingUtils.fromUuid;
import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.killrvideo.service.rating.dto.VideoRating;
import com.killrvideo.service.rating.dto.VideoRatingByUser;

import com.killrvideo.service.rating.request.GetUserRatingRequestData;
import com.killrvideo.utils.GrpcMappingUtils;
import killrvideo.ratings.RatingsServiceOuterClass.*;
import killrvideo.ratings.events.RatingsEvents;
import org.springframework.stereotype.Component;

/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
public class RatingsServiceGrpcMapper {
     
   /**
     * Mapping to generated GPRC beans.
     */
    public GetRatingResponse mapToRatingResponse(VideoRating vr) {
        return GetRatingResponse.newBuilder()
                .setVideoId(uuidToUuid(vr.getVideoid()))
                .setRatingsCount(Optional.ofNullable(vr.getRatingCounter()).orElse(0L))
                .setRatingsTotal(Optional.ofNullable(vr.getRatingTotal()).orElse(0L))
                .build();
    }
    
    /**
     * Mapping to generated GPRC beans.
     */
    public GetUserRatingResponse mapToUserRatingResponse(VideoRatingByUser vr) {
        return GetUserRatingResponse.newBuilder()
                .setVideoId(uuidToUuid(vr.getVideoid()))
                .setUserId(uuidToUuid(vr.getUserid()))
                .setRating(vr.getRating())
                .build();
    }

    public GetUserRatingRequestData parseGetUserRatingRequest(GetUserRatingRequest grpcReq) {
        UUID videoid = fromUuid(grpcReq.getVideoId());
        UUID userid = fromUuid(grpcReq.getUserId());

        return new GetUserRatingRequestData(videoid, userid);
    }

    public VideoRatingByUser parseRateVideoRequest(RateVideoRequest grpcReq) {
        UUID videoid = fromUuid(grpcReq.getVideoId());
        UUID userid  = fromUuid(grpcReq.getUserId());
        int rate = grpcReq.getRating();

        return new VideoRatingByUser(videoid, userid, rate);
    }

    public RatingsEvents.UserRatedVideo createUserRatedVideoEvent(VideoRatingByUser rating) {
        return RatingsEvents.UserRatedVideo.newBuilder()
                .setRating(rating.getRating())
                .setRatingTimestamp(GrpcMappingUtils.instantToTimeStamp(Instant.now()))
                .setUserId(uuidToUuid(rating.getUserid()))
                .setVideoId(uuidToUuid(rating.getVideoid()))
                .build();
    }
}
