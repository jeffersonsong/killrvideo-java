package com.killrvideo.service.rating.grpc

import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.rating.dto.VideoRatingByUser
import com.killrvideo.service.rating.grpc.RatingsServiceGrpcMapper.GetUserRatingRequestExtensions.parse
import com.killrvideo.service.rating.grpc.RatingsServiceGrpcMapper.RateVideoRequestExtensions.parse
import com.killrvideo.service.rating.repository.RatingRepository
import com.killrvideo.service.rating.request.GetUserRatingRequestData
import com.killrvideo.service.utils.ServiceGrpcUtils.trace
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.ratings.RatingsServiceGrpc.RatingsServiceImplBase
import killrvideo.ratings.RatingsServiceGrpcKt
import killrvideo.ratings.RatingsServiceOuterClass.*
import killrvideo.ratings.getRatingResponse
import killrvideo.ratings.getUserRatingResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

/**
 * Operations on Ratings with GRPC.
 *
 * @author DataStax advocates Team
 */
@Service
class RatingsServiceGrpc(
    private val ratingRepository: RatingRepository,
    private val messagingDao: MessagingDao,
    private val validator: RatingsServiceGrpcValidator,
    private val mapper: RatingsServiceGrpcMapper
) : RatingsServiceGrpcKt.RatingsServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return current value of 'serviceKey'
     */
    @Value("\${killrvideo.discovery.services.rating : RatingsService}")
    val serviceKey: String? = null

    @Value("\${killrvideo.messaging.kafka.topics.videoRated : topic-kv-videoRating}")
    private val topicvideoRated: String? = null

    /**
     * {@inheritDoc}
     */
    override suspend fun rateVideo(request: RateVideoRequest): RateVideoResponse {
        // Validate Parameters
        validator.validateGrpcRequest_RateVideo(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val videoRatingByUser: VideoRatingByUser = request.parse()

        // Invoking Dao (Async), publish event if successful
        return runCatching { ratingRepository.rateVideo(videoRatingByUser) }
            .map {
                RateVideoResponse.newBuilder().build()
            }.onSuccess {
                messagingDao.sendEvent(topicvideoRated, mapper.createUserRatedVideoEvent(videoRatingByUser))
            }.trace(logger, "rateVideo", starts).getOrThrow()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getRating(request: GetRatingRequest): GetRatingResponse {
        // Validate Parameters
        validator.validateGrpcRequest_GetRating(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val videoid = fromUuid(request.videoId)

        // Invoking Dao (Async) and map result back to GRPC (maptoRatingResponse)
        return runCatching { ratingRepository.findRating(videoid) }
            .map { vr ->
                vr?.let { mapper.mapToRatingResponse(it) } ?: emptyRatingResponse(videoid)
            }.trace(logger, "getRating", starts)
            .getOrThrow()
    }

    private fun emptyRatingResponse(videoid: UUID): GetRatingResponse =
        getRatingResponse {
            videoId = uuidToUuid(videoid)
            ratingsCount = 0L
            ratingsTotal = 0L
        }

    /**
     * {@inheritDoc}
     */
    override suspend fun getUserRating(grpcReq: GetUserRatingRequest): GetUserRatingResponse {
        // Validate Parameters
        validator.validateGrpcRequest_GetUserRating(grpcReq)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val requestData = grpcReq.parse()

        // Invoking Dao (Async) and map result back to GRPC (maptoRatingResponse)
        return runCatching { ratingRepository.findUserRating(requestData) }
            .map { ur ->
                ur?.let { mapper.mapToUserRatingResponse(it) } ?: emptyUserRatingResponse(requestData)
            }.trace(logger, "getUserRating", starts)
            .getOrThrow()
    }

    private fun emptyUserRatingResponse(request: GetUserRatingRequestData): GetUserRatingResponse =
        getUserRatingResponse {
            userId = uuidToUuid(request.userid)
            videoId = uuidToUuid(request.videoid)
            rating = 0
        }
}
