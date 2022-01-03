package com.killrvideo.service.comment.repository

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.dse.utils.PageableQueryFactory
import com.killrvideo.service.comment.dao.CommentByUserDao
import com.killrvideo.service.comment.dao.CommentByVideoDao
import com.killrvideo.service.comment.dao.CommentMapper
import com.killrvideo.service.comment.dao.CommentRowMapper
import com.killrvideo.service.comment.dto.*
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Repository
import java.util.concurrent.CompletableFuture

/**
 * Implementation of queries and related to [Comment] objects within DataStax Enterprise.
 * Comments are store in 2 tables and all queries are performed against Apache Cassandra.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
class CommentRepository(
    pageableQueryFactory: PageableQueryFactory,
    mapper: CommentMapper,
    commentRowMapper: CommentRowMapper
) {
    private val commentByUserDao: CommentByUserDao = mapper.commentByUserDao
    private val commentByVideoDao: CommentByVideoDao = mapper.commentByVideoDao
    private val findCommentsByUser: PageableQuery<Comment>
    private val findCommentsByVideo: PageableQuery<Comment>

    init {
        findCommentsByUser = pageableQueryFactory.newPageableQuery(
            QUERY_COMMENTS_BY_USERID,
            ConsistencyLevel.LOCAL_ONE
        ) { commentRowMapper.map(it) }
        findCommentsByVideo = pageableQueryFactory.newPageableQuery(
            QUERY_COMMENTS_BY_VIDEOID,
            ConsistencyLevel.LOCAL_ONE
        ) { commentRowMapper.map(it) }
    }

    /**
     * Insert a comment for a video. (in multiple table at once). When executing query async result will be a completable future.
     * Note the 'executeAsync'> No result are expected from insertion, and we return CompletableFuture<VOID>.
     *
     * @param comment comment to be inserted by signup user.
    </VOID> */
    suspend fun insertCommentAsync(comment: Comment): Comment =
        CompletableFuture.allOf(
            commentByUserDao.insert(CommentByUser.from(comment)),
            commentByVideoDao.insert(CommentByVideo.from(comment))
        ).thenApply { comment }.await()

    /**
     * Search comment_by_video Asynchronously with Pagination.
     */
    suspend fun findCommentsByVideosIdAsync(query: QueryCommentByVideo): ResultListPage<Comment> =
        if (query.commentId != null) {
            commentByVideoDao.find(query.videoId, query.commentId)
                .thenApply { it?.toComment() }
                .thenApply { ResultListPage.fromNullable(it) }.await()
        } else {
            findCommentsByVideo.queryNext(
                query.pageSize,
                query.pageState,
                query.videoId
            ).await()
        }

    /**
     * Execute a query against the 'comment_by_user' table (ASYNC).
     */
    suspend fun findCommentsByUserIdAsync(query: QueryCommentByUser): ResultListPage<Comment> =
        if (query.commentId != null) {
            commentByUserDao.find(query.userId, query.commentId)
                .thenApply { it?.toComment() }
                .thenApply { ResultListPage.fromNullable(it) }.await()
        } else {
            findCommentsByUser.queryNext(
                query.pageSize,
                query.pageState,
                query.userId
            ).await()
        }

    companion object {
        private val QUERY_COMMENTS_BY_USERID =
            """
            SELECT userid, commentid, videoid, comment, toTimestamp(commentid) as comment_timestamp 
            FROM killrvideo.comments_by_user 
            WHERE userid = :userid
            """.trimIndent()

        private val QUERY_COMMENTS_BY_VIDEOID =
            """
            SELECT videoid, commentid, userid, comment, toTimestamp(commentid) as comment_timestamp 
            FROM killrvideo.comments_by_video 
            WHERE videoid = :videoid
            """.trimIndent()
    }
}
