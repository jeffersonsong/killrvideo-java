package com.killrvideo.service.comment.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.comment.dto.Comment
import com.killrvideo.service.comment.repository.CommentRepository
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
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
    private val serviceKey = "CommentsService"
    private val messageDestination = "topic-kv-commentCreation"

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testCommentOnVideoWithValidationFailed() {
        val request = commentOnVideoRequest {}

        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } throws IllegalArgumentException()

        assertThrows<IllegalArgumentException> {
            runBlocking {
                service.commentOnVideo(request)
            }
        }
    }

    @Test
    fun testCommentOnVideoAllWithInsertFailed() {
        val request = commentOnVideoRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } just Runs

        val event = userCommentedOnVideo {}
        every { mapper.createUserCommentedOnVideoEvent(any())} returns event
        coEvery { commentRepository.insertCommentAsync(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.commentOnVideo(request) }
        }

        verify(exactly = 0) { messagingDao.sendEvent(any(), any()) }
    }

    @Test
    fun testCommentOnVideoAllWithSendFailed() {
        val request = commentOnVideoRequest {}
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
            runBlocking { service.commentOnVideo(request) }
        }
    }

    @Test
    fun testCommentOnVideo() {
        val request = commentOnVideoRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } just Runs
        val comment = mockk<Comment>()
        val event = userCommentedOnVideo {}

        every {mapper.createUserCommentedOnVideoEvent(any())} returns event
        coEvery { commentRepository.insertCommentAsync(any()) } returns comment
        every {messagingDao.sendEvent(any(), any()) } returns
                CompletableFuture.completedFuture(null)
        runBlocking { service.commentOnVideo(request) }
    }

    @Test
    fun testGetUserCommentsWithValidationFailed() {
        val request = getUserCommentsRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getUserComments(request) }
        }
    }

    @Test
    fun testGetUserCommentsWithQueryFailed() {
        val request = getUserCommentsRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } just Runs

        coEvery {
            commentRepository.findCommentsByUserIdAsync(any())
        } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.getUserComments(request) }
        }
    }

    @Test
    fun testGetUserComments() {
        val request = getUserCommentsRequest {
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

        val result = runBlocking { service.getUserComments(request) }
        assertEquals(response, result)
    }

    @Test
    fun testGetVideoCommentsWithValidationFailed() {
        val request = getVideoCommentsRequest {}
        every {
            validator.validateGrpcRequestCommentOnVideo(any())
        } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getVideoComments(request) }
        }
    }

    @Test
    fun testGetVideoCommentsWithQueryFailed() {
        val request = getVideoCommentsRequest {}
        every { validator.validateGrpcRequestGetVideoComment(any()) } just Runs
        coEvery {
            commentRepository.findCommentsByVideosIdAsync(any())
        } throws Exception()

        assertThrows<Exception> {
            runBlocking {
                service.getVideoComments(request)
            }
        }
    }

    @Test
    fun testGetVideoComments() {
        val request = getVideoCommentsRequest {
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
            service.getVideoComments(request)
        }

        assertEquals(response, result)
    }
}
