package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper.GetRelatedVideosRequestExtensions.parse
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository
import com.killrvideo.service.utils.ServiceGrpcUtils.trace
import com.killrvideo.utils.GrpcMappingUtils
import killrvideo.suggested_videos.SuggestedVideoServiceGrpcKt
import killrvideo.suggested_videos.SuggestedVideosService.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Suggested video for a user.
 *
 * @author DataStax advocates Team
 */
@Service
class SuggestedVideosServiceGrpc(
    private val suggestedVideosRepository: SuggestedVideosRepository,
    private val validator: SuggestedVideosServiceGrpcValidator,
    private val mapper: SuggestedVideosServiceGrpcMapper,
    @Value("\${killrvideo.discovery.services.suggestedVideo : SuggestedVideoService}")
    val serviceKey: String
) : SuggestedVideoServiceGrpcKt.SuggestedVideoServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger { }
    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return
     * current value of 'serviceKey'
     */


    /** {@inheritDoc}  */
    override suspend fun getRelatedVideos(grpcReq: GetRelatedVideosRequest): GetRelatedVideosResponse {
        // Validate Parameters
        validator.validateGrpcRequest_getRelatedVideo(grpcReq)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val requestData = grpcReq.parse()

        // Invoke DAO Async
        return runCatching { suggestedVideosRepository.getRelatedVideos(requestData) }
            .map { mapper.mapToGetRelatedVideosResponse(it, requestData.videoid) }
            .trace(logger, "getRelatedVideos", starts)
            .getOrThrow()
    }

    /** {@inheritDoc}  */
    override suspend fun getSuggestedForUser(grpcReq: GetSuggestedForUserRequest): GetSuggestedForUserResponse {
        // Validate Parameters
        validator.validateGrpcRequest_getUserSuggestedVideo(grpcReq)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val userid = GrpcMappingUtils.fromUuid(grpcReq.userId)

        // Invoke DAO Async
        return kotlin.runCatching { suggestedVideosRepository.getSuggestedVideosForUser(userid) }
            .map {
                mapper.mapToGetSuggestedForUserResponse(userid, it)
            }.trace(logger, "getSuggestedForUser", starts)
            .getOrThrow()
    }
}