package com.killrvideo.service.comment.grpc;

import com.killrvideo.utils.FluentValidator;
import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * GRPC Requests Validation Utility class : Implements controls before use request and throw
 * errors if parameter are invalid.
 *
 * @author DataStax Developer Advocates team.
 */
@Component
public class CommentsServiceGrpcValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentsServiceGrpcValidator.class);

    /**
     * Validate comment On video comment query.
     *
     * @param request        current GRPC Request
     * @param streamObserver response async
     */
    public void validateGrpcRequestCommentOnVideo(CommentOnVideoRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("commentOnVideo", request, LOGGER, streamObserver)
                .notEmpty("userId", !request.hasUserId() || isBlank(request.getUserId().getValue()))
                .notEmpty("videoId", !request.hasVideoId() || isBlank(request.getVideoId().getValue()))
                .notEmpty("commentId", !request.hasCommentId() || isBlank(request.getCommentId().getValue()))
                .notEmpty( "comment", isBlank(request.getComment()))
                .validate();
    }

    /**
     * Validate get video comment query.
     *
     * @param request        current GRPC Request
     * @param streamObserver response async
     */
    public void validateGrpcRequestGetVideoComment(GetVideoCommentsRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getVideoComments", request,  LOGGER, streamObserver)
                .notEmpty("video id",
                        !request.hasVideoId() || isBlank(request.getVideoId().getValue()))
                .positive("page size", request.getPageSize() <= 0)
                .validate();
    }

    /**
     * Validate get user comment query.
     *
     * @param request        current GRPC Request
     * @param streamObserver response async
     */
    public void validateGrpcRequest_GetUserComments(GetUserCommentsRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("getUserComments", request, LOGGER, streamObserver)
                .notEmpty("userId", !request.hasUserId() || isBlank(request.getUserId().getValue()))
                .positive("page size", request.getPageSize() <= 0)
                .validate();
    }
}
