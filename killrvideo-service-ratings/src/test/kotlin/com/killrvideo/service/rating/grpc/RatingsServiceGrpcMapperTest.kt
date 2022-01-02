package com.killrvideo.service.rating.grpc

import com.killrvideo.service.rating.dto.VideoRating
import com.killrvideo.service.rating.dto.VideoRatingByUser
import com.killrvideo.service.rating.grpc.RatingsServiceGrpcMapper.GetUserRatingRequestExtensions.parse
import com.killrvideo.service.rating.grpc.RatingsServiceGrpcMapper.RateVideoRequestExtensions.parse
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.ratings.getUserRatingRequest
import killrvideo.ratings.rateVideoRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

internal class RatingsServiceGrpcMapperTest {
    private val mapper = RatingsServiceGrpcMapper()
    @Test
    fun testMapToRatingResponse() {
        val videoid = UUID.randomUUID()
        val vr = VideoRating(videoid = videoid)
        val proto = mapper.mapToRatingResponse(vr)
        assertEquals(vr.videoid.toString(), proto.videoId.value)
        assertEquals(0L, proto.ratingsCount)
        assertEquals(0L, proto.ratingsTotal)
    }

    @Test
    fun testMapToUserRatingResponse() {
        val videoid = UUID.randomUUID()
        val userid = UUID.randomUUID()
        val vr = VideoRatingByUser(videoid = videoid, userid=userid, rating = 4)
        val proto = mapper.mapToUserRatingResponse(vr)
        assertEquals(vr.videoid.toString(), proto.videoId.value)
        assertEquals(vr.userid.toString(), proto.userId.value)
        assertEquals(4, proto.rating)
    }

    @Test
    fun testParseGetUserRatingRequest() {
        val videoid = UUID.randomUUID()
        val userid = UUID.randomUUID()
        val request = getUserRatingRequest {
            videoId = uuidToUuid(videoid)
            userId = uuidToUuid(userid)
        }
        val (videoid1, userid1) = request.parse()
        assertEquals(videoid, videoid1)
        assertEquals(userid, userid1)
    }

    @Test
    fun testParseRateVideoRequest() {
        val videoid = UUID.randomUUID()
        val userid = UUID.randomUUID()
        val request = rateVideoRequest {
            videoId = uuidToUuid(videoid)
            userId = uuidToUuid(userid)
        }
        val (videoid1, userid1) = request.parse()
        assertEquals(videoid, videoid1)
        assertEquals(userid, userid1)
    }

    @Test
    fun testCreateUserRatedVideoEvent() {
        val videoid = UUID.randomUUID()
        val userid = UUID.randomUUID()
        val vr = VideoRatingByUser(videoid = videoid, userid=userid, rating = 4)
        val event = mapper.createUserRatedVideoEvent(vr)
        assertEquals(videoid.toString(), event.videoId.value)
        assertEquals(userid.toString(), event.userId.value)
        assertEquals(4, event.rating)
        assertNotNull(event.ratingTimestamp)
    }
}
