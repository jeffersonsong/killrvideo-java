package com.killrvideo.service.comment.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.comment.dto.*;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CompletableFuture;

import static com.killrvideo.service.comment.dto.Comment.*;
import static com.killrvideo.service.comment.dto.Comment.COLUMN_COMMENT;
import static com.killrvideo.utils.PageableQueryUtils.*;

/**
 * Implementation of queries and related to {@link Comment} objects within DataStax Enterprise.
 * Comments are store in 2 tables and all queries are performed against Apache Cassandra.
 *
 * @author Jefferson Song
 */
@Repository
public class CommentRepository {
    private static final String QUERY_COMMENTS_BY_USER =
            "SELECT userid, commentid, videoid, comment, toTimestamp(commentid) as comment_timestamp " +
                    "FROM killrvideo.comments_by_user " +
                    "WHERE userid = ?";
    public static final String QUERY_COMMENTS_BY_VIDEO =
            "SELECT videoid, commentid, userid, comment, toTimestamp(commentid) as comment_timestamp " +
                    "FROM killrvideo.comments_by_video " +
                    "WHERE videoid = ?";
    private final CqlSession session;
    private final CommentByUserDao commentByUserDao;
    private final CommentByVideoDao commentByVideoDao;
    private final PreparedStatement findCommentsByUser;
    private final PreparedStatement findCommentsByVideo;

    public CommentRepository(CqlSession session) {
        this.session = session;
        CommentMapper mapper = CommentMapper.build(session).build();
        this.commentByUserDao = mapper.getCommentByUserDao();
        this.commentByVideoDao = mapper.getCommentByVideoDao();

        findCommentsByUser = session.prepare(QUERY_COMMENTS_BY_USER);
        findCommentsByVideo = session.prepare(QUERY_COMMENTS_BY_VIDEO);
    }

    public CompletableFuture<Void> insertCommentAsync(final Comment comment) {
        CommentByUser commentByUser = new CommentByUser(comment);
        CommentByVideo commentByVideo = new CommentByVideo(comment);

        CompletableFuture<Void> future1 = commentByUserDao.insert(commentByUser);
        CompletableFuture<Void> future2 = commentByVideoDao.insert(commentByVideo);


        return CompletableFuture.allOf(future1, future2);
    }

    /**
     * Search comment_by_video Asynchronously with Pagination.
     */
    public CompletableFuture<ResultListPage<Comment>> findCommentsByVideosIdAsync(final QueryCommentByVideo query) {
        if (query.getCommentId().isPresent()) {
            return commentByVideoDao.find(query.getVideoId(), query.getCommentId().get())
                    .thenApply(s -> singleElementResult(s.toComment()));
        } else {
            BoundStatement boundStatement =
                    buildStatement(
                            findCommentsByVideo,
                            stmt -> stmt.boundStatementBuilder(query.getVideoId()),
                            query.getPageSize(),
                            query.getPageState(),
                            ConsistencyLevel.QUORUM
                    );
            return queryAsyncWithPagination(session, boundStatement, CommentRepository::mapToComment);
        }
    }

    /**
     * Execute a query against the 'comment_by_user' table (ASYNC).
     */
    public CompletableFuture<ResultListPage<Comment>> findCommentsByUserIdAsync(final QueryCommentByUser query) {
        if (query.getCommentId().isPresent()) {
            return commentByUserDao.find(query.getUserId(), query.getCommentId().get())
                    .thenApply(s -> singleElementResult(s.toComment()));
        } else {
            BoundStatement boundStatement =
                    buildStatement(
                            findCommentsByUser,
                            stmt -> stmt.boundStatementBuilder(query.getUserId()),
                            query.getPageSize(),
                            query.getPageState(),
                            ConsistencyLevel.QUORUM
                    );
            return queryAsyncWithPagination(session, boundStatement, CommentRepository::mapToComment);
        }
    }

    private static Comment mapToComment(Row row) {
        Comment c = new Comment();
        c.setComment(row.getString(COLUMN_COMMENT));
        c.setUserid(row.getUuid(COLUMN_USERID));
        c.setCommentid(row.getUuid(COLUMN_COMMENTID));
        c.setVideoid(row.getUuid(COLUMN_VIDEOID));
        c.setDateOfComment(row.getInstant("comment_timestamp"));
        return c;
    }
}
