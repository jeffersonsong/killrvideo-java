package com.killrvideo.service.comment.grpc

import com.killrvideo.utils.GrpcMappingUtils.randomTimeUuid
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import killrvideo.comments.commentOnVideoRequest
import killrvideo.comments.getUserCommentsRequest
import killrvideo.comments.getVideoCommentsRequest
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
        assertThrows<IllegalArgumentException> {
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
        assertThrows<IllegalArgumentException> {
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
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_GetUserComments(request)
        }
    }
}
