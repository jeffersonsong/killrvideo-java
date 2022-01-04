package com.killrvideo.service.rating.grpc

import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.rating.dto.VideoRatingByUser
import com.killrvideo.service.rating.grpc.RatingsServiceGrpcMapper.GetUserRatingRequestExtensions.parse
import com.killrvideo.service.rating.grpc.RatingsServiceGrpcMapper.RateVideoRequestExtensions.parse
import com.killrvideo.service.rating.repository.RatingRepository
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import killrvideo.ratings.RatingsServiceGrpcKt
import killrvideo.ratings.RatingsServiceOuterClass.*
import killrvideo.ratings.rateVideoResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

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
    private val mapper: RatingsServiceGrpcMapper,
    @Value("\${killrvideo.discovery.services.rating : RatingsService}")
    val serviceKey: String,
    @Value("\${killrvideo.messaging.kafka.topics.videoRated : topic-kv-videoRating}")
    private val topicvideoRated: String
) : RatingsServiceGrpcKt.RatingsServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    /**
     * {@inheritDoc}
     */
    override suspend fun rateVideo(request: RateVideoRequest): RateVideoResponse {
        // Validate Parameters
        validator.validateGrpcRequest_RateVideo(request)

        // Mapping GRPC => Domain (Dao)
        val videoRatingByUser: VideoRatingByUser = request.parse()

        // Invoking Dao (Async), publish event if successful
        return runCatching { ratingRepository.rateVideo(videoRatingByUser) }
            .map { rateVideoResponse {}}
            .onSuccess {
                notifyUserRatedVideo(videoRatingByUser)
            }.getOrThrow()
    }

    private fun notifyUserRatedVideo(videoRatingByUser: VideoRatingByUser) =
        messagingDao.sendEvent(topicvideoRated, mapper.createUserRatedVideoEvent(videoRatingByUser))

    /**
     * {@inheritDoc}
     */
    override suspend fun getRating(request: GetRatingRequest): GetRatingResponse {
        // Validate Parameters
        validator.validateGrpcRequest_GetRating(request)

        // Mapping GRPC => Domain (Dao)
        val videoid = fromUuid(request.videoId)

        // Invoking Dao (Async) and map result back to GRPC (maptoRatingResponse)
        return runCatching { ratingRepository.findRating(videoid) }
            .map { mapper.mapToRatingResponse(it) }
            .getOrThrow()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getUserRating(request: GetUserRatingRequest): GetUserRatingResponse {
        // Validate Parameters
        validator.validateGrpcRequest_GetUserRating(request)

        // Mapping GRPC => Domain (Dao)
        val requestData = request.parse()

        // Invoking Dao (Async) and map result back to GRPC (maptoRatingResponse)
        return runCatching { ratingRepository.findUserRating(requestData) }
            .map { mapper.mapToUserRatingResponse(it) }
            .getOrThrow()
    }
}
