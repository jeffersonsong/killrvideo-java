package com.killrvideo.service.comment.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.comment.dao.CommentByUserDao;
import com.killrvideo.service.comment.dao.CommentByVideoDao;
import com.killrvideo.service.comment.dao.CommentMapper;
import com.killrvideo.service.comment.dao.CommentRowMapper;
import com.killrvideo.service.comment.dto.*;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CompletableFuture;

import static com.killrvideo.dse.utils.PageableQueryUtils.*;

/**
 * Implementation of queries and related to {@link Comment} objects within DataStax Enterprise.
 * Comments are store in 2 tables and all queries are performed against Apache Cassandra.
 *
 * @author Jefferson Song
 */
@Repository
public class CommentRepository {
    private final CqlSession session;
    private final CommentByUserDao commentByUserDao;
    private final CommentByVideoDao commentByVideoDao;
    private final PreparedStatement findCommentsByUser;
    private final PreparedStatement findCommentsByVideo;
    private final CommentRowMapper commentRowMapper;

    public CommentRepository(CqlSession session, CommentRowMapper commentRowMapper) {
        this.session = session;
        this.commentRowMapper = commentRowMapper;
        CommentMapper mapper = CommentMapper.build(session).build();
        this.commentByUserDao = mapper.getCommentByUserDao();
        this.commentByVideoDao = mapper.getCommentByVideoDao();

        findCommentsByUser = session.prepare(
                "SELECT userid, commentid, videoid, comment, toTimestamp(commentid) as comment_timestamp " +
                "FROM killrvideo.comments_by_user " +
                "WHERE userid = :userid"
        );
        findCommentsByVideo = session.prepare(
                "SELECT videoid, commentid, userid, comment, toTimestamp(commentid) as comment_timestamp " +
                        "FROM killrvideo.comments_by_video " +
                        "WHERE videoid = :videoid"
        );
    }

    public CompletableFuture<Void> insertCommentAsync(final Comment comment) {
        CommentByUser commentByUser = new CommentByUser(comment);
        CommentByVideo commentByVideo = new CommentByVideo(comment);

        return CompletableFuture.allOf(
                commentByUserDao.insert(commentByUser),
                commentByVideoDao.insert(commentByVideo)
        );
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
                            ConsistencyLevel.LOCAL_ONE
                    );
            return queryAsyncWithPagination(session, boundStatement, commentRowMapper::map);
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
                            ConsistencyLevel.LOCAL_ONE
                    );
            return queryAsyncWithPagination(session, boundStatement, commentRowMapper::map);
        }
    }
}
