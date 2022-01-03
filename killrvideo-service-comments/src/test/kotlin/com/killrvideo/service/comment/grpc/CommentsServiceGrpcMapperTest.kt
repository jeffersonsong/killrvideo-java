package com.killrvideo.service.comment.grpc

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.comment.dto.Comment
import com.killrvideo.service.comment.grpc.CommentsServiceGrpcMapper.CommentOnVideoRequestExtensions.parse
import com.killrvideo.service.comment.grpc.CommentsServiceGrpcMapper.GetUserCommentsRequestExtensions.parse
import com.killrvideo.service.comment.grpc.CommentsServiceGrpcMapper.GetVideoCommentsRequestExtensions.parse
import com.killrvideo.utils.GrpcMappingUtils.uuidToTimeUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.comments.CommentsServiceOuterClass.*
import killrvideo.comments.commentOnVideoRequest
import killrvideo.comments.getUserCommentsRequest
import killrvideo.comments.getVideoCommentsRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class CommentsServiceGrpcMapperTest {
    private val mapper = CommentsServiceGrpcMapper()

    @Test
    fun testMapFromGrpcUserCommentToDseQuery() {
        val userId = UUID.randomUUID()
        val startingCommentId = Uuids.timeBased()
        val request = getUserCommentsRequestWithStartingCommentIdAndState(userId, startingCommentId)
        val pojo = request.parse()
        assertEquals(userId, pojo.userId)
        assertNotNull(pojo.commentId)
        assertEquals(startingCommentId, pojo.commentId)
        assertEquals(5, pojo.pageSize)
        assertNotNull(pojo.pageState)
        assertEquals("paging state", pojo.pageState)
    }

    @Test
    fun testMapFromGrpcUserCommentToDseQueryWithoutCommentId() {
        val userId = UUID.randomUUID()
        val request = getUserCommentsRequestInitial(userId)
        val pojo = request.parse()
        assertEquals(userId, pojo.userId)
        assertNull(pojo.commentId)
        assertEquals(5, pojo.pageSize)
        assertNull(pojo.pageState)
    }

    @Test
    fun testMapFromDseVideoCommentToGrpcResponse() {
        val comment = comment()
        val comments = ResultListPage(listOf(comment), null)
        val proto = mapper.mapFromDseVideoCommentToGrpcResponse(comments)
        assertEquals(comment.videoid.toString(), proto.videoId.value)
        assertEquals(1, proto.commentsCount)
    }

    @Test
    fun testMapFromDseUserCommentToGrpcResponse() {
        val comment = comment()
        val comments = ResultListPage(listOf(comment), null)
        val proto = mapper.mapFromDseUserCommentToGrpcResponse(comments)
        assertEquals(comment.userid.toString(), proto.userId.value)
        assertEquals(1, proto.commentsCount)
    }

    @Test
    fun testMapFromGrpcVideoCommentToDseQuery() {
        val videoid = UUID.randomUUID()
        val startingCommentId = Uuids.timeBased()
        val request = getVideoCommentsRequestWithStartingCommentIdAndState(videoid, startingCommentId)
        val pojo = request.parse()
        assertEquals(videoid, pojo.videoId)
        assertNotNull(pojo.commentId)
        assertEquals(startingCommentId, pojo.commentId)
        assertEquals(5, pojo.pageSize)
        assertNotNull(pojo.pageState)
        assertEquals("paging state", pojo.pageState)
    }

    @Test
    fun testMapFromGrpcVideoCommentToDseQueryWithoutCommentId() {
        val videoid = UUID.randomUUID()
        val request = getVideoCommentsRequestInitial(videoid)
        val pojo = request.parse()
        assertEquals(videoid, pojo.videoId)
        assertEquals(5, pojo.pageSize)
        assertNull(pojo.pageState)
    }

    @Test
    fun testMapToComment() {
        val vidoid = UUID.randomUUID()
        val userid = UUID.randomUUID()
        val commentid = Uuids.timeBased()
        val commentText = "Test"
        val request = createCommentOnVideoRequest(vidoid, userid, commentid, commentText)
        val pojo = request.parse()
        assertEquals(vidoid, pojo.videoid)
        assertEquals(userid, pojo.userid)
        assertEquals(commentid, pojo.commentid)
        assertEquals(commentText, pojo.comment)
    }

    @Test
    fun testCreateUserCommentedOnVideoEvent() {
        val comment = comment()
        val event = mapper.createUserCommentedOnVideoEvent(comment)
        assertEquals(comment.commentid.toString(), event.commentId.value)
        assertEquals(comment.videoid.toString(), event.videoId.value)
        assertEquals(comment.userid.toString(), event.userId.value)
        assertNotNull(event.commentTimestamp)
    }

    private fun getUserCommentsRequestInitial(userid: UUID): GetUserCommentsRequest =
        getUserCommentsRequest {
            userId = uuidToUuid(userid)
            pageSize = 5
        }

    private fun getUserCommentsRequestWithStartingCommentIdAndState(
        userid: UUID,
        startingCommentid: UUID
    ): GetUserCommentsRequest =
        getUserCommentsRequest {
            startingCommentId = uuidToTimeUuid(startingCommentid)
            userId = uuidToUuid(userid)
            pageSize = 5
            pagingState = "paging state"
        }

    private fun getVideoCommentsRequestWithStartingCommentIdAndState(
        videoid: UUID,
        startingCommentid: UUID
    ): GetVideoCommentsRequest =
        getVideoCommentsRequest {
            startingCommentId = uuidToTimeUuid(startingCommentid)
            videoId = uuidToUuid(videoid)
            pageSize = 5
            pagingState = "paging state"
        }

    private fun getVideoCommentsRequestInitial(videoid: UUID): GetVideoCommentsRequest =
        getVideoCommentsRequest {
            videoId = uuidToUuid(videoid)
            pageSize = 5
        }

    private fun createCommentOnVideoRequest(
        vidoid: UUID,
        userid: UUID,
        commentid: UUID,
        commentText: String
    ): CommentOnVideoRequest =
        commentOnVideoRequest {
            videoId = uuidToUuid(vidoid)
            commentId = uuidToTimeUuid(commentid)
            userId = uuidToUuid(userid)
            comment = commentText
        }

    private fun comment(): Comment =
        Comment(
            comment = "test",
            commentid = Uuids.timeBased(),
            userid = UUID.randomUUID(),
            videoid = UUID.randomUUID(),
            dateOfComment = Instant.now()
        )
}
