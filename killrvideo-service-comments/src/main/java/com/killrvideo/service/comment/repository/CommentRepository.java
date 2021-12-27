package com.killrvideo.service.comment.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.service.comment.dao.CommentByUserDao;
import com.killrvideo.service.comment.dao.CommentByVideoDao;
import com.killrvideo.service.comment.dao.CommentMapper;
import com.killrvideo.service.comment.dao.CommentRowMapper;
import com.killrvideo.service.comment.dto.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of queries and related to {@link Comment} objects within DataStax Enterprise.
 * Comments are store in 2 tables and all queries are performed against Apache Cassandra.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
public class CommentRepository {
    private static final String QUERY_COMMENTS_BY_USERID =
            "SELECT userid, commentid, videoid, comment, toTimestamp(commentid) as comment_timestamp " +
                    "FROM killrvideo.comments_by_user " +
                    "WHERE userid = :userid";
    public static final String QUERY_COMMENTS_BY_VIDEOID =
            "SELECT videoid, commentid, userid, comment, toTimestamp(commentid) as comment_timestamp " +
                    "FROM killrvideo.comments_by_video " +
                    "WHERE videoid = :videoid";

    private final CommentByUserDao commentByUserDao;
    private final CommentByVideoDao commentByVideoDao;
    private final PageableQuery<Comment> findCommentsByUser;
    private final PageableQuery<Comment> findCommentsByVideo;

    public CommentRepository(CqlSession session, CommentRowMapper commentRowMapper) {
        CommentMapper mapper = CommentMapper.build(session).build();
        this.commentByUserDao = mapper.getCommentByUserDao();
        this.commentByVideoDao = mapper.getCommentByVideoDao();

        findCommentsByUser = new PageableQuery<>(
                QUERY_COMMENTS_BY_USERID,
                session,
                ConsistencyLevel.LOCAL_ONE,
                commentRowMapper::map
        );
        findCommentsByVideo = new PageableQuery<>(
                QUERY_COMMENTS_BY_VIDEOID,
                session,
                ConsistencyLevel.LOCAL_ONE,
                commentRowMapper::map
        );
    }

    /**
     * Insert a comment for a video. (in multiple table at once). When executing query async result will be a completable future.
     * Note the 'executeAsync'> No result are expected from insertion and we return CompletableFuture<VOID>.
     *
     * @param comment comment to be inserted by signup user.
     */
    public CompletableFuture<Comment> insertCommentAsync(final Comment comment) {
        return CompletableFuture.allOf(
                commentByUserDao.insert(CommentByUser.from(comment)),
                commentByVideoDao.insert(CommentByVideo.from(comment))
        ).thenApply(rs -> comment);
    }

    /**
     * Search comment_by_video Asynchronously with Pagination.
     */
    public CompletableFuture<ResultListPage<Comment>> findCommentsByVideosIdAsync(final QueryCommentByVideo query) {
        if (query.getCommentId().isPresent()) {
            return commentByVideoDao.find(query.getVideoId(), query.getCommentId().get())
                    .thenApply(CommentByVideo::toComment)
                    .thenApply(ResultListPage::from);
        } else {
            return findCommentsByVideo.queryNext(
                    Optional.of(query.getPageSize()),
                    query.getPageState(),
                    query.getVideoId()
            );
        }
    }

    /**
     * Execute a query against the 'comment_by_user' table (ASYNC).
     */
    public CompletableFuture<ResultListPage<Comment>> findCommentsByUserIdAsync(final QueryCommentByUser query) {
        if (query.getCommentId().isPresent()) {
            return commentByUserDao.find(query.getUserId(), query.getCommentId().get())
                    .thenApply(CommentByUser::toComment)
                    .thenApply(ResultListPage::from);
        } else {
            return findCommentsByUser.queryNext(
                    Optional.of(query.getPageSize()),
                    query.getPageState(),
                    query.getUserId()
            );
        }
    }
}
