package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper.GetRelatedVideosRequestExtensions.parse
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository
import com.killrvideo.utils.GrpcMappingUtils
import killrvideo.suggested_videos.SuggestedVideoServiceGrpcKt
import killrvideo.suggested_videos.SuggestedVideosService.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

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

    /** {@inheritDoc}  */
    override suspend fun getRelatedVideos(request: GetRelatedVideosRequest): GetRelatedVideosResponse {
        // Validate Parameters
        validator.validateGrpcRequest_getRelatedVideo(request)

        // Mapping GRPC => Domain (Dao)
        val requestData = request.parse()

        // Invoke DAO Async
        return runCatching { suggestedVideosRepository.getRelatedVideos(requestData) }
            .map { mapper.mapToGetRelatedVideosResponse(it, requestData.videoid) }
            .getOrThrow()
    }

    /** {@inheritDoc}  */
    override suspend fun getSuggestedForUser(request: GetSuggestedForUserRequest): GetSuggestedForUserResponse {
        // Validate Parameters
        validator.validateGrpcRequest_getUserSuggestedVideo(request)

        // Mapping GRPC => Domain (Dao)
        val userid = GrpcMappingUtils.fromUuid(request.userId)

        // Invoke DAO Async
        return kotlin.runCatching { suggestedVideosRepository.getSuggestedVideosForUser(userid) }
            .map {
                mapper.mapToGetSuggestedForUserResponse(userid, it)
            }
            .getOrThrow()
    }
}
