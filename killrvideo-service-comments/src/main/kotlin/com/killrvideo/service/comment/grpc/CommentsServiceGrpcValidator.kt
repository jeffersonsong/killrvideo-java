package com.killrvideo.service.comment.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.comments.CommentsServiceOuterClass.*
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * GRPC Requests Validation Utility class : Implements controls before use request and throw
 * errors if parameter are invalid.
 *
 * @author DataStax Developer Advocates team.
 */
@Component
class CommentsServiceGrpcValidator {
    /**
     * Validate comment On video comment query.
     *
     * @param request        current GRPC Request
     * @param streamObserver response async
     */
    fun validateGrpcRequestCommentOnVideo(request: CommentOnVideoRequest) {
        FluentValidator.of("commentOnVideo", request, LOGGER)
            .notEmpty("userId", !request.hasUserId() || StringUtils.isBlank(request.userId.value))
            .notEmpty("videoId", !request.hasVideoId() || StringUtils.isBlank(request.videoId.value))
            .notEmpty("commentId", !request.hasCommentId() || StringUtils.isBlank(request.commentId.value))
            .notEmpty("comment", StringUtils.isBlank(request.comment))
            .validate()
    }

    /**
     * Validate get video comment query.
     *
     * @param request        current GRPC Request
     * @param streamObserver response async
     */
    fun validateGrpcRequestGetVideoComment(request: GetVideoCommentsRequest) {
        FluentValidator.of("getVideoComments", request, LOGGER)
            .notEmpty(
                "video id",
                !request.hasVideoId() || StringUtils.isBlank(request.videoId.value)
            )
            .positive("page size", request.pageSize <= 0)
            .validate()
    }

    /**
     * Validate get user comment query.
     *
     * @param request        current GRPC Request
     * @param streamObserver response async
     */
    fun validateGrpcRequest_GetUserComments(request: GetUserCommentsRequest) {
        FluentValidator.of("getUserComments", request, LOGGER)
            .notEmpty("userId", !request.hasUserId() || StringUtils.isBlank(request.userId.value))
            .positive("page size", request.pageSize <= 0)
            .validate()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CommentsServiceGrpcValidator::class.java)
    }
}