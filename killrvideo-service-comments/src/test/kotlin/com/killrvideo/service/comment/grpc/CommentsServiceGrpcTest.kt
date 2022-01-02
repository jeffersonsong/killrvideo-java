package com.killrvideo.service.comment.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.comment.dto.Comment
import com.killrvideo.service.comment.repository.CommentRepository
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse
import killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse
import killrvideo.comments.commentOnVideoRequest
import killrvideo.comments.events.userCommentedOnVideo
import killrvideo.comments.getUserCommentsRequest
import killrvideo.comments.getVideoCommentsRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CompletableFuture

class CommentsServiceGrpcTest {
    @InjectMockKs
    private lateinit var service: CommentsServiceGrpc

    @MockK
    private lateinit var commentRepository: CommentRepository

    @MockK
    private lateinit var messagingDao: MessagingDao

    @MockK
    private lateinit var validator: CommentsServiceGrpcValidator

    @MockK
    private lateinit var mapper: CommentsServiceGrpcMapper

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testCommentOnVideoWithValidationFailed() {
        val grpcReq = commentOnVideoRequest {}

        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } throws Status.INVALID_ARGUMENT.asRuntimeException()

        assertThrows<StatusRuntimeException> {
            runBlocking {
                service.commentOnVideo(grpcReq)
            }
        }
    }

    @Test
    fun testCommentOnVideoAllWithInsertFailed() {
        val grpcReq = commentOnVideoRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } just Runs

        val event = userCommentedOnVideo {}
        every { mapper.createUserCommentedOnVideoEvent(any())} returns event
        coEvery { commentRepository.insertCommentAsync(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.commentOnVideo(grpcReq) }
        }

        verify(exactly = 0) { messagingDao.sendEvent(any(), any()) }
    }

    @Test
    fun testCommentOnVideoAllWithSendFailed() {
        val grpcReq = commentOnVideoRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } just Runs
        val comment = mockk<Comment>()
        val event = userCommentedOnVideo {}

        every {mapper.createUserCommentedOnVideoEvent(any())} returns event
        coEvery { commentRepository.insertCommentAsync(any()) } returns comment
        val future: CompletableFuture<Void> = CompletableFuture.failedFuture(Exception())
        every {messagingDao.sendEvent(any(), any()) } returns future

        assertThrows<Exception> {
            runBlocking { service.commentOnVideo(grpcReq) }
        }
    }

    @Test
    fun testCommentOnVideo() {
        val grpcReq = commentOnVideoRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } just Runs
        val comment = mockk<Comment>()
        val event = userCommentedOnVideo {}

        every {mapper.createUserCommentedOnVideoEvent(any())} returns event
        coEvery { commentRepository.insertCommentAsync(any()) } returns comment
        every {messagingDao.sendEvent(any(), any()) } returns
                CompletableFuture.completedFuture(null)
        runBlocking { service.commentOnVideo(grpcReq) }
    }

    @Test
    fun testGetUserCommentsWithValidationFailed() {
        val grpcReq = getUserCommentsRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getUserComments(grpcReq) }
        }
    }

    @Test
    fun testGetUserCommentsWithQueryFailed() {
        val grpcReq = getUserCommentsRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } just Runs

        coEvery {
            commentRepository.findCommentsByUserIdAsync(any())
        } throws Exception()
        assertThrows<Exception> {
            runBlocking { service!!.getUserComments(grpcReq) }
        }
    }

    @Test
    fun testGetUserComments() {
        val grpcReq = getUserCommentsRequest {
            userId = randomUuid()
            pageSize = 2
        }
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } just Runs
        val resultListPage: ResultListPage<Comment> = mockk()
        coEvery {
            commentRepository.findCommentsByUserIdAsync(any())
        } returns resultListPage

        val response = mockk<GetUserCommentsResponse>()
        every { mapper.mapFromDseUserCommentToGrpcResponse(any()) } returns response

        val result = runBlocking { service.getUserComments(grpcReq) }
        assertEquals(response, result)
    }

    @Test
    fun testGetVideoCommentsWithValidationFailed() {
        val grpcReq = getVideoCommentsRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getVideoComments(grpcReq) }
        }
    }

    @Test
    fun testGetVideoCommentsWithQueryFailed() {
        val grpcReq = getVideoCommentsRequest {}
        every { validator.validateGrpcRequestGetVideoComment(any()) } just Runs
        coEvery {
            commentRepository.findCommentsByVideosIdAsync(any())
        } throws Exception()

        assertThrows<Exception> {
            runBlocking {
                service.getVideoComments(grpcReq)
            }
        }
    }

    @Test
    fun testGetVideoComments() {
        val grpcReq = getVideoCommentsRequest {
            videoId = randomUuid()
            pageSize = 2
        }

        every { validator.validateGrpcRequestGetVideoComment(any()) } just Runs

        val resultListPage: ResultListPage<Comment> = mockk()
        coEvery {
            commentRepository.findCommentsByVideosIdAsync(any())
        } returns resultListPage
        val response = mockk<GetVideoCommentsResponse>()
        every { mapper.mapFromDseVideoCommentToGrpcResponse(any()) } returns response
        val result = runBlocking {
            service.getVideoComments(grpcReq)
        }

        assertEquals(response, result)
    }
}
