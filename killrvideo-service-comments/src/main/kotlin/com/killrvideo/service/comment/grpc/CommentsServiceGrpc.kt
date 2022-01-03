package com.killrvideo.service.comment.grpc

import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.comment.dto.Comment
import com.killrvideo.service.comment.grpc.CommentsServiceGrpcMapper.CommentOnVideoRequestExtensions.parse
import com.killrvideo.service.comment.grpc.CommentsServiceGrpcMapper.GetUserCommentsRequestExtensions.parse
import com.killrvideo.service.comment.grpc.CommentsServiceGrpcMapper.GetVideoCommentsRequestExtensions.parse
import com.killrvideo.service.comment.repository.CommentRepository
import com.killrvideo.service.utils.ServiceGrpcUtils.trace
import killrvideo.comments.CommentsServiceGrpcKt
import killrvideo.comments.CommentsServiceOuterClass.*
import killrvideo.comments.commentOnVideoResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Exposition of comment services with GPRC Technology & Protobuf Interface
 *
 * @author DataStax advocates team.
 */
@Service
class CommentsServiceGrpc(
    /**
     * Communications and queries to DSE (Comment).
     */
    private val commentRepository: CommentRepository,
    private val messagingDao: MessagingDao,
    private val validator: CommentsServiceGrpcValidator,
    private val mapper: CommentsServiceGrpcMapper
) : CommentsServiceGrpcKt.CommentsServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}
    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return current value of 'serviceKey'
     */
    @Value("\${killrvideo.discovery.services.comment : CommentsService}")
    val serviceKey: String? = null

    @Value("\${killrvideo.messaging.destinations.commentCreated : topic-kv-commentCreation}")
    private val messageDestination: String? = null

    /**
     * {@inheritDoc}
     */
    override suspend fun commentOnVideo(request: CommentOnVideoRequest):CommentOnVideoResponse  {
        // Boilerplate Code for validation delegated to {@link CommentsServiceGrpcValidator}
        validator.validateGrpcRequestCommentOnVideo(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val comment = request.parse()
        logger.debug {"Insert comment on video ${comment.videoid} for user ${comment.userid} : $comment" }

        return runCatching { commentRepository.insertCommentAsync(comment) }
            .map { rs: Comment ->
                messagingDao.sendEvent(messageDestination, mapper.createUserCommentedOnVideoEvent(rs)).get()
                rs
            }.map {
                commentOnVideoResponse {}
            }.trace(logger, "commentOnVideo", starts).getOrThrow()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getVideoComments(request: GetVideoCommentsRequest): GetVideoCommentsResponse {
        // Parameter validations
        validator.validateGrpcRequestGetVideoComment(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao) : Dedicated bean creating for flexibility
        val query = request.parse()

        // ASYNCHRONOUS works with ComputableFuture
        return runCatching { commentRepository.findCommentsByVideosIdAsync(query) }
            .map { mapper.mapFromDseVideoCommentToGrpcResponse(it) }
            .onFailure { error ->
                messagingDao.sendErrorEvent(serviceKey, error)
            }.trace(logger, "getVideoComments", starts).getOrThrow()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getUserComments(request: GetUserCommentsRequest): GetUserCommentsResponse {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_GetUserComments(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao) : Dedicated bean creating for flexibility
        val query = request.parse()
        logger.debug { "Listing comment for user ${query.userId}" }

        // ASYNCHRONOUS works with ComputableFuture
        return runCatching { commentRepository.findCommentsByUserIdAsync(query) }
            .map { mapper.mapFromDseUserCommentToGrpcResponse(it) }
            .onFailure { error ->
                messagingDao.sendErrorEvent(serviceKey, error)
            }.trace(logger, "getUserComments", starts).getOrThrow()
    }
}