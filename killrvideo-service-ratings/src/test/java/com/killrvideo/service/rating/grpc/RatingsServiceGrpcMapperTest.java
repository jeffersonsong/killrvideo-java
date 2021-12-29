package com.killrvideo.service.rating.grpc;

import com.killrvideo.service.rating.dto.VideoRating;
import com.killrvideo.service.rating.dto.VideoRatingByUser;
import com.killrvideo.service.rating.request.GetUserRatingRequestData;
import killrvideo.ratings.RatingsServiceOuterClass.*;
import killrvideo.ratings.events.RatingsEvents;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static org.junit.jupiter.api.Assertions.*;

class RatingsServiceGrpcMapperTest {
    private RatingsServiceGrpcMapper mapper = new RatingsServiceGrpcMapper();

    @Test
    public void testMapToRatingResponse() {
        UUID videoid = UUID.randomUUID();
        VideoRating vr = new VideoRating(videoid);
        GetRatingResponse proto = mapper.mapToRatingResponse(vr);
        assertEquals(vr.getVideoid().toString(), proto.getVideoId().getValue());
        assertEquals(0L, proto.getRatingsCount());
        assertEquals(0L, proto.getRatingsTotal());
    }

    @Test
    public void testMapToUserRatingResponse() {
        UUID videoid = UUID.randomUUID();
        UUID userid = UUID.randomUUID();
        VideoRatingByUser vr = new VideoRatingByUser(videoid, userid, 4);
        GetUserRatingResponse proto = mapper.mapToUserRatingResponse(vr);
        assertEquals(vr.getVideoid().toString(), proto.getVideoId().getValue());
        assertEquals(vr.getUserid().toString(), proto.getUserId().getValue());
        assertEquals(4, proto.getRating());
    }

    @Test
    public void testParseGetUserRatingRequest() {
        UUID videoid = UUID.randomUUID();
        UUID userid = UUID.randomUUID();
        GetUserRatingRequest request = GetUserRatingRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .setUserId(uuidToUuid(userid))
                .build();

        GetUserRatingRequestData pojo = mapper.parseGetUserRatingRequest(request);
        assertEquals(videoid, pojo.getVideoid());
        assertEquals(userid, pojo.getUserid());
    }

    @Test
    public void testParseRateVideoRequest() {
        UUID videoid = UUID.randomUUID();
        UUID userid = UUID.randomUUID();
        RateVideoRequest request = RateVideoRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .setUserId(uuidToUuid(userid))
                .build();

        VideoRatingByUser pojo = mapper.parseRateVideoRequest(request);
        assertEquals(videoid, pojo.getVideoid());
        assertEquals(userid, pojo.getUserid());
    }

    @Test
    public void testCreateUserRatedVideoEvent() {
        UUID videoid = UUID.randomUUID();
        UUID userid = UUID.randomUUID();
        VideoRatingByUser vr = new VideoRatingByUser(videoid, userid, 4);
        RatingsEvents.UserRatedVideo event = mapper.createUserRatedVideoEvent(vr);
        assertEquals(videoid.toString(), event.getVideoId().getValue());
        assertEquals(userid.toString(), event.getUserId().getValue());
        assertEquals(4, event.getRating());
        assertNotNull(event.getRatingTimestamp());
    }
}