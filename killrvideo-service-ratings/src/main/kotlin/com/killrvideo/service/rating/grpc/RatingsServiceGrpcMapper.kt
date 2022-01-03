package com.killrvideo.service.rating.grpc

import com.killrvideo.service.rating.dto.VideoRating
import com.killrvideo.service.rating.dto.VideoRatingByUser
import com.killrvideo.service.rating.request.GetUserRatingRequestData
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import com.killrvideo.utils.GrpcMappingUtils.instantToTimeStamp
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.ratings.RatingsServiceOuterClass.*
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo
import killrvideo.ratings.getRatingResponse
import killrvideo.ratings.getUserRatingResponse
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
class RatingsServiceGrpcMapper {
    object GetUserRatingRequestExtensions {
        fun GetUserRatingRequest.parse(): GetUserRatingRequestData =
            GetUserRatingRequestData(
                userid = fromUuid(this.userId),
                videoid = fromUuid(this.videoId)
            )
    }

    object RateVideoRequestExtensions {
        fun RateVideoRequest.parse(): VideoRatingByUser =
            VideoRatingByUser(
                videoid = fromUuid(this.videoId),
                userid = fromUuid(this.userId),
                rating = this.rating
            )
    }

    /**
     * Mapping to generated GPRC beans.
     */
    fun mapToRatingResponse(vr: VideoRating): GetRatingResponse =
        getRatingResponse {
            vr.videoid?.let { videoId = uuidToUuid(it) }
            ratingsCount = vr.ratingCounter
            ratingsTotal = vr.ratingTotal
        }

    /**
     * Mapping to generated GPRC beans.
     */
    fun mapToUserRatingResponse(vr: VideoRatingByUser): GetUserRatingResponse =
        getUserRatingResponse {
            vr.videoid?.let { videoId = uuidToUuid(it) }
            vr.userid?.let { userId = uuidToUuid(it) }
            rating = vr.rating
        }


    fun createUserRatedVideoEvent(rating: VideoRatingByUser): UserRatedVideo {
        val builder = UserRatedVideo.newBuilder()
            .setRating(rating.rating)
            .setRatingTimestamp(instantToTimeStamp(Instant.now()))

        rating.userid?.let { builder.setUserId(uuidToUuid(it)) }
        rating.videoid?.let { builder.setVideoId(uuidToUuid(it)) }

        return builder.build()
    }
}
