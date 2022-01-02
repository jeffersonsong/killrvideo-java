package com.killrvideo.service.comment.grpc

import com.killrvideo.utils.GrpcMappingUtils
import com.killrvideo.utils.GrpcMappingUtils.randomTimeUuid
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import io.grpc.StatusRuntimeException
import killrvideo.comments.CommentsServiceOuterClass.*
import killrvideo.comments.commentOnVideoRequest
import killrvideo.comments.getUserCommentsRequest
import killrvideo.comments.getVideoCommentsRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CommentsServiceGrpcValidatorTest {
    private val validator = CommentsServiceGrpcValidator()

    @Test
    fun testValidateGrpcRequestCommentOnVideoSuccess() {
        val request = commentOnVideoRequest {
            userId = randomUuid()
            videoId= randomUuid()
            commentId = randomTimeUuid()
            comment = "Comment"
        }
        validator.validateGrpcRequestCommentOnVideo(request)
    }

    @Test
    fun testValidateGrpcRequestCommentOnVideoFailed() {
        val request = commentOnVideoRequest {}
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequestCommentOnVideo(request)
        }
    }

    @Test
    fun testValidateGrpcRequestGetVideoCommentSuccess() {
        val request = getVideoCommentsRequest {
            videoId= randomUuid()
            pageSize = 2
        }
        validator.validateGrpcRequestGetVideoComment(request)
    }

    @Test
    fun testValidateGrpcRequestGetVideoCommentFailed() {
        val request = getVideoCommentsRequest {}
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequestGetVideoComment(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_GetUserCommentsSuccess() {
        val request = getUserCommentsRequest {
            userId = randomUuid()
            pageSize = 2
        }
        validator.validateGrpcRequest_GetUserComments(request)
    }

    @Test
    fun testValidateGrpcRequest_GetUserCommentsFailed() {
        val request =getUserCommentsRequest {}
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequest_GetUserComments(request)
        }
    }
}
