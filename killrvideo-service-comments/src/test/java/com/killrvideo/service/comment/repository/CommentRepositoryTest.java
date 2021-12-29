package com.killrvideo.service.comment.repository;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;
import com.killrvideo.service.comment.dao.CommentByUserDao;
import com.killrvideo.service.comment.dao.CommentByVideoDao;
import com.killrvideo.service.comment.dao.CommentMapper;
import com.killrvideo.service.comment.dao.CommentRowMapper;
import com.killrvideo.service.comment.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentRepositoryTest {
    private CommentRepository repository;

    private PageableQuery<Comment> findCommentsByUser;
    private PageableQuery<Comment> findCommentsByVideo;

    private CommentByUserDao commentByUserDao;
    private CommentByVideoDao commentByVideoDao;

    @BeforeEach
    public void openMocks() {
        PageableQueryFactory pageableQueryFactory = mock(PageableQueryFactory.class);
        findCommentsByUser = mock(PageableQuery.class);
        findCommentsByVideo = mock(PageableQuery.class);
        when(pageableQueryFactory.newPageableQuery(any(), any(), (Function<Row, Comment>) any()))
                .thenReturn(findCommentsByUser, findCommentsByVideo);

        CommentMapper mapper = mock(CommentMapper.class);
        commentByUserDao = mock(CommentByUserDao.class);
        commentByVideoDao = mock(CommentByVideoDao.class);
        when(mapper.getCommentByUserDao()).thenReturn(commentByUserDao);
        when(mapper.getCommentByVideoDao()).thenReturn(commentByVideoDao);

        CommentRowMapper commentRowMapper = mock(CommentRowMapper.class);

        repository = new CommentRepository(pageableQueryFactory, mapper, commentRowMapper);
    }

    @Test
    void testInsertCommentAsync() throws Exception {
        when(commentByUserDao.insert(any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );
        when(commentByVideoDao.insert(any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        Comment comment = comment();
        repository.insertCommentAsync(comment).whenComplete((result, error) -> {
                    assertEquals(comment, result);
                    assertNull(error);
                });
    }

    @Test
    void testInsertCommentAsyncWithOneOfInsertFailed() throws Exception {
        when(commentByUserDao.insert(any())).thenReturn(
                CompletableFuture.failedFuture(new Exception())
        );
        when(commentByVideoDao.insert(any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        Comment comment = comment();
        repository.insertCommentAsync(comment).whenComplete((result, error) -> {
            assertNotNull(error);
            assertNull(result);
        });
    }

    @Test
    void testFindCommentsByVideosIdAsyncWithCommentId() {
        Comment comment = comment();
        CommentByVideo commentByVideo = CommentByVideo.from(comment);
        when(commentByVideoDao.find(any(), any())).thenReturn(
                CompletableFuture.completedFuture(commentByVideo)
        );

        QueryCommentByVideo query = queryCommentByVideoWithCommentId(comment, 5);
        repository.findCommentsByVideosIdAsync(query).whenComplete((result, error) -> {
            assertEquals(1, result.getResults().size());
            assertEquals(comment.getCommentid(), result.getResults().get(0).getCommentid());
            assertNull(error);
        });
        verify(commentByVideoDao, times(1)).find(any(), any());
    }

    @Test
    void testFindCommentsByVideosIdAsyncWithoutCommentId() {
        Comment comment = comment();
        ResultListPage<Comment> resultListPage = ResultListPage.from(comment);
        QueryCommentByVideo query = queryCommentByVideoWithoutCommentId(comment, 5);
        when(findCommentsByVideo.queryNext(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(resultListPage));

        repository.findCommentsByVideosIdAsync(query).whenComplete((result, error) -> {
            assertEquals(resultListPage, result);
            assertNull(error);
        });
        verify(commentByVideoDao, times(0)).find(any(), any());
        verify(findCommentsByVideo, times(1)).queryNext(any(), any(), any());
    }

    @Test
    void testFindCommentsByUserIdAsyncWithCommentId() {
        Comment comment = comment();
        CommentByUser commentByUser = CommentByUser.from(comment);
        when(commentByUserDao.find(any(), any())).thenReturn(
                CompletableFuture.completedFuture(commentByUser)
        );

        QueryCommentByUser query = queryCommentByUserWithCommentId(comment, 5);
        repository.findCommentsByUserIdAsync(query).whenComplete((result, error) -> {
            assertEquals(1, result.getResults().size());
            assertEquals(comment.getCommentid(), result.getResults().get(0).getCommentid());
            assertNull(error);
        });
        verify(commentByUserDao, times(1)).find(any(), any());
    }

    @Test
    void testFindCommentsByUserIdAsyncWithoutCommentId() {
        Comment comment = comment();
        ResultListPage<Comment> resultListPage = ResultListPage.from(comment);
        when(findCommentsByUser.queryNext(any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(resultListPage)
        );

        QueryCommentByUser query = queryCommentByUserWithoutCommentId(comment, 5);
        repository.findCommentsByUserIdAsync(query).whenComplete((result, error) -> {
            assertEquals(resultListPage, result);
            assertNull(error);
        });
        verify(commentByUserDao, times(0)).find(any(), any());
        verify(findCommentsByUser, times(1)).queryNext(any(), any(), any());
    }

    private QueryCommentByVideo queryCommentByVideoWithCommentId(Comment comment, int pageSize) {
        return new QueryCommentByVideo(
                comment.getVideoid(),
                Optional.of(comment.getCommentid()),
                pageSize,
                Optional.empty()
        );
    }

    private QueryCommentByVideo queryCommentByVideoWithoutCommentId(Comment comment, int pageSize) {
        return new QueryCommentByVideo(
                comment.getVideoid(),
                Optional.empty(),
                pageSize,
                Optional.empty()
        );
    }

    private QueryCommentByUser queryCommentByUserWithCommentId(Comment comment, int pageSize) {
        return new QueryCommentByUser(
                comment.getUserid(),
                Optional.of(comment.getCommentid()),
                pageSize,
                Optional.empty()
        );
    }

    private QueryCommentByUser queryCommentByUserWithoutCommentId(Comment comment, int pageSize) {
        return new QueryCommentByUser(
                comment.getUserid(),
                Optional.empty(),
                pageSize,
                Optional.empty()
        );
    }

    private Comment comment() {
        Comment comment = new Comment();
        comment.setUserid(UUID.randomUUID());
        comment.setVideoid(UUID.randomUUID());
        comment.setCommentid(Uuids.timeBased());
        comment.setComment("Test");
        comment.setDateOfComment(Instant.now());

        return comment;
    }
}