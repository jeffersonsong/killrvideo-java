package com.killrvideo.service.comment.repository

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.service.comment.dao.CommentByUserDao
import com.killrvideo.service.comment.dao.CommentByVideoDao
import com.killrvideo.service.comment.dao.CommentMapper
import com.killrvideo.service.comment.dao.CommentRowMapper
import com.killrvideo.service.comment.dto.*
import com.killrvideo.utils.test.CassandraTestUtilsKt.mockPageableQueryFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

internal class CommentRepositoryTest {
    private lateinit var repository: CommentRepository
    private lateinit var findCommentsByUser: PageableQuery<Comment>
    private lateinit var findCommentsByVideo: PageableQuery<Comment>
    private lateinit var commentByUserDao: CommentByUserDao
    private lateinit var commentByVideoDao: CommentByVideoDao

    @BeforeEach
    fun setUp() {
        findCommentsByUser = mockk()
        findCommentsByVideo = mockk()
        val pageableQueryFactory = mockPageableQueryFactory(findCommentsByUser, findCommentsByVideo)
        val mapper = mockk<CommentMapper>()

        commentByUserDao = mockk()
        commentByVideoDao = mockk()
        every { mapper.commentByUserDao } returns commentByUserDao
        every { mapper.commentByVideoDao } returns commentByVideoDao
        val commentRowMapper = mockk<CommentRowMapper>()
        repository = CommentRepository(pageableQueryFactory, mapper, commentRowMapper)
    }

    @Test
    fun testInsertCommentAsync() {
        every { commentByUserDao.insert(any()) } returns CompletableFuture.completedFuture(null)
        every { commentByVideoDao.insert(any()) } returns CompletableFuture.completedFuture(null)

        val comment = comment()
        val result = runBlocking { repository.insertCommentAsync(comment) }
        assertEquals(comment, result)
    }

    @Test
    fun testInsertCommentAsyncWithOneOfInsertFailed() {
        every { commentByUserDao.insert(any()) } returns CompletableFuture.failedFuture(Exception())
        every { commentByVideoDao.insert(any()) } returns CompletableFuture.completedFuture(null)

        val comment = comment()
        assertThrows<Exception> {
            runBlocking { repository.insertCommentAsync(comment) }
        }
    }

    @Test
    fun testFindCommentsByVideosIdAsyncWithCommentId() {
        val comment = comment()
        val commentByVideo = CommentByVideo.from(comment)
        every { commentByVideoDao.find(any(), any()) } returns
                CompletableFuture.completedFuture(commentByVideo)

        val query = queryCommentByVideoWithCommentId(comment, 5)

        val result = runBlocking { repository.findCommentsByVideosIdAsync(query) }
        assertEquals(1, result.results.size)
        assertEquals(comment.commentid, result.results[0].commentid)

        verify(exactly = 1) { commentByVideoDao.find(any(), any()) }
    }

    @Test
    fun testFindCommentsByVideosIdAsyncWithoutCommentId() {
        val comment = comment()
        val resultListPage = ResultListPage.from(comment)
        val query = queryCommentByVideoWithoutCommentId(comment, 5)
        every {
            findCommentsByVideo.queryNext(any(), any(), any())
        } returns CompletableFuture.completedFuture(resultListPage)

        val result = runBlocking { repository.findCommentsByVideosIdAsync(query) }
        assertEquals(resultListPage.results.size, result.results.size)
        verify(exactly = 0) {
            commentByVideoDao.find(any(), any())
        }
        verify(exactly = 1) {
            findCommentsByVideo.queryNext(any(), any(), any())
        }
    }

    @Test
    fun testFindCommentsByUserIdAsyncWithCommentId() {
        val comment = comment()
        val commentByUser = CommentByUser.from(comment)
        every { commentByUserDao.find(any(), any()) } returns
                CompletableFuture.completedFuture(commentByUser)

        val query = queryCommentByUserWithCommentId(comment, 5)
        val result = runBlocking { repository.findCommentsByUserIdAsync(query) }

        assertEquals(1, result.results.size)
        assertEquals(comment.commentid, result.results[0].commentid)

        verify(exactly = 1) {
            commentByUserDao.find(any(), any())
        }
    }

    @Test
    fun testFindCommentsByUserIdAsyncWithoutCommentId() {
        val comment = comment()
        val resultListPage = ResultListPage.from(comment)
        every {
            findCommentsByUser.queryNext(any(), any(), any())
        } returns CompletableFuture.completedFuture(resultListPage)

        val query = queryCommentByUserWithoutCommentId(comment, 5)
        val result = runBlocking { repository.findCommentsByUserIdAsync(query) }
        assertEquals(resultListPage.results.size, result.results.size)
        verify(exactly = 0) { commentByUserDao.find(any(), any()) }
        verify(exactly = 1) { findCommentsByUser.queryNext(any(), any(), any()) }
    }

    private fun queryCommentByVideoWithCommentId(comment: Comment, pageSize: Int): QueryCommentByVideo =
        QueryCommentByVideo(
            videoId = comment.videoid!!,
            commentId = comment.commentid,
            pageSize = pageSize,
            pageState = null
        )

    private fun queryCommentByVideoWithoutCommentId(comment: Comment, pageSize: Int): QueryCommentByVideo =
        QueryCommentByVideo(
            videoId = comment.videoid!!,
            commentId = null,
            pageSize = pageSize,
            pageState = null
        )

    private fun queryCommentByUserWithCommentId(comment: Comment, pageSize: Int): QueryCommentByUser =
        QueryCommentByUser(
            userId = comment.userid!!,
            commentId = comment.commentid,
            pageSize = pageSize,
            pageState = null
        )

    private fun queryCommentByUserWithoutCommentId(comment: Comment, pageSize: Int): QueryCommentByUser =
        QueryCommentByUser(
            userId = comment.userid!!,
            commentId = null,
            pageSize = pageSize,
            pageState = null
        )

    private fun comment(): Comment =
        Comment(
            userid = UUID.randomUUID(),
            videoid = UUID.randomUUID(),
            commentid = Uuids.timeBased(),
            comment = "Test",
            dateOfComment = Instant.now()
        )
}
