package com.killrvideo.service.comment.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.comments.CommentsServiceOuterClass.*
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

/**
 * GRPC Requests Validation Utility class : Implements controls before use request and throw
 * errors if parameter are invalid.
 *
 * @author DataStax Developer Advocates team.
 */
@Component
class CommentsServiceGrpcValidator {
    private val logger = KotlinLogging.logger {  }
    /**
     * Validate comment On video comment query.
     *
     * @param request        current GRPC Request
     */
    fun validateGrpcRequestCommentOnVideo(request: CommentOnVideoRequest) =
        FluentValidator.of("commentOnVideo", request, logger)
            .notEmpty("userId", !request.hasUserId() || StringUtils.isBlank(request.userId.value))
            .notEmpty("videoId", !request.hasVideoId() || StringUtils.isBlank(request.videoId.value))
            .notEmpty("commentId", !request.hasCommentId() || StringUtils.isBlank(request.commentId.value))
            .notEmpty("comment", StringUtils.isBlank(request.comment))
            .validate()

    /**
     * Validate get video comment query.
     *
     * @param request        current GRPC Request
     */
    fun validateGrpcRequestGetVideoComment(request: GetVideoCommentsRequest) =
        FluentValidator.of("getVideoComments", request, logger)
            .notEmpty("video id", !request.hasVideoId() || StringUtils.isBlank(request.videoId.value))
            .positive("page size", request.pageSize <= 0)
            .validate()

    /**
     * Validate get user comment query.
     *
     * @param request        current GRPC Request
     */
    fun validateGrpcRequest_GetUserComments(request: GetUserCommentsRequest) =
        FluentValidator.of("getUserComments", request, logger)
            .notEmpty("userId", !request.hasUserId() || StringUtils.isBlank(request.userId.value))
            .positive("page size", request.pageSize <= 0)
            .validate()
}
