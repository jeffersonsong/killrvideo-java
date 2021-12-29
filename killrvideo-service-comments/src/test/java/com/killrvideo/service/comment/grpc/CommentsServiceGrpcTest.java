package com.killrvideo.service.comment.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.comment.dto.Comment;
import com.killrvideo.service.comment.dto.QueryCommentByUser;
import com.killrvideo.service.comment.dto.QueryCommentByVideo;
import com.killrvideo.service.comment.repository.CommentRepository;
import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceOuterClass.*;
import killrvideo.comments.events.CommentsEvents;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class CommentsServiceGrpcTest {
    @InjectMocks
    private CommentsServiceGrpc service;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private MessagingDao messagingDao;
    @Mock
    private CommentsServiceGrpcValidator validator;
    @Mock
    private CommentsServiceGrpcMapper mapper;

    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    public void testCommentOnVideoWithValidationFailed() {
        CommentOnVideoRequest grpcReq = CommentOnVideoRequest.getDefaultInstance();
        StreamObserver<CommentOnVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(validator).validateGrpcRequestCommentOnVideo(any(),any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.commentOnVideo(grpcReq, grpcResObserver)
        );
    }

    @Test
    public void testCommentOnVideoAllWithInsertFailed() {
        CommentOnVideoRequest grpcReq = CommentOnVideoRequest.getDefaultInstance();
        StreamObserver<CommentOnVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequestCommentOnVideo(any(),any());
        Comment comment = mock(Comment.class);
        CommentsEvents.UserCommentedOnVideo event = CommentsEvents.UserCommentedOnVideo.getDefaultInstance();

        when(mapper.mapToComment(any())).thenReturn(comment);
        when(mapper.createUserCommentedOnVideoEvent(any())).thenReturn(event);
        when(commentRepository.insertCommentAsync(comment))
                .thenReturn(CompletableFuture.failedFuture(new Exception()));

        service.commentOnVideo(grpcReq, grpcResObserver);
        verify(messagingDao, times(0)).sendEvent(any(), any());
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    public void testCommentOnVideoAllWithSendFailed() {
        CommentOnVideoRequest grpcReq = CommentOnVideoRequest.getDefaultInstance();
        StreamObserver<CommentOnVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequestCommentOnVideo(any(),any());
        Comment comment = mock(Comment.class);
        CommentsEvents.UserCommentedOnVideo event = CommentsEvents.UserCommentedOnVideo.getDefaultInstance();

        when(mapper.mapToComment(any())).thenReturn(comment);
        when(mapper.createUserCommentedOnVideoEvent(any())).thenReturn(event);
        when(commentRepository.insertCommentAsync(comment))
                .thenReturn(CompletableFuture.completedFuture(comment));
        when(messagingDao.sendEvent(any(), any())).thenReturn(CompletableFuture.failedFuture(new Exception()));

        service.commentOnVideo(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    public void testCommentOnVideo() {
        CommentOnVideoRequest grpcReq = CommentOnVideoRequest.getDefaultInstance();
        StreamObserver<CommentOnVideoResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequestCommentOnVideo(any(),any());
        Comment comment = mock(Comment.class);
        CommentsEvents.UserCommentedOnVideo event = CommentsEvents.UserCommentedOnVideo.getDefaultInstance();

        when(mapper.mapToComment(any())).thenReturn(comment);
        when(mapper.createUserCommentedOnVideoEvent(any())).thenReturn(event);
        when(commentRepository.insertCommentAsync(comment))
                .thenReturn(CompletableFuture.completedFuture(comment));
        when(messagingDao.sendEvent(any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        service.commentOnVideo(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    public void testGetUserCommentsWithValidationFailed() {
        GetUserCommentsRequest grpcReq = GetUserCommentsRequest.getDefaultInstance();
        StreamObserver<GetUserCommentsResponse> responseObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(validator).validateGrpcRequest_GetUserComments(any(),any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.getUserComments(grpcReq, responseObserver)
        );
    }

    @Test
    public void testGetUserCommentsWithQueryFailed() {
        GetUserCommentsRequest grpcReq = GetUserCommentsRequest.getDefaultInstance();
        StreamObserver<GetUserCommentsResponse> responseObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequest_GetUserComments(any(),any());
        QueryCommentByUser query = mock(QueryCommentByUser.class);
        when(mapper.mapFromGrpcUserCommentToDseQuery(any())).thenReturn(query);
        when(commentRepository.findCommentsByUserIdAsync(any())).thenReturn(CompletableFuture.failedFuture(new Exception()));

        this.service.getUserComments(grpcReq, responseObserver);
        verify(responseObserver, times(1)).onError(any());
        verify(responseObserver, times(0)).onNext(any());
        verify(responseObserver, times(0)).onCompleted();
    }

    @Test
    public void testGetUserComments() {
        GetUserCommentsRequest grpcReq = GetUserCommentsRequest.getDefaultInstance();
        StreamObserver<GetUserCommentsResponse> responseObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequest_GetUserComments(any(),any());
        QueryCommentByUser query = mock(QueryCommentByUser.class);
        when(mapper.mapFromGrpcUserCommentToDseQuery(any())).thenReturn(query);

        ResultListPage<Comment> resultListPage = mock(ResultListPage.class);
        when(commentRepository.findCommentsByUserIdAsync(any())).thenReturn(
                CompletableFuture.completedFuture(resultListPage)
        );

        this.service.getUserComments(grpcReq, responseObserver);
        verify(responseObserver, times(0)).onError(any());
        verify(responseObserver, times(1)).onNext(any());
        verify(responseObserver, times(1)).onCompleted();
    }
    @Test
    public void testGetVideoCommentsWithValidationFailed() {
        GetVideoCommentsRequest grpcReq = GetVideoCommentsRequest.getDefaultInstance();
        StreamObserver<GetVideoCommentsResponse> responseObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(validator).validateGrpcRequestGetVideoComment(any(),any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.getVideoComments(grpcReq, responseObserver)
        );
    }

    @Test
    public void testGetVideoCommentsWithQueryFailed() {
        GetVideoCommentsRequest grpcReq = GetVideoCommentsRequest.getDefaultInstance();
        StreamObserver<GetVideoCommentsResponse> responseObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequestGetVideoComment(any(),any());
        QueryCommentByVideo query = mock(QueryCommentByVideo.class);
        when(mapper.mapFromGrpcVideoCommentToDseQuery(any())).thenReturn(query);
        when(commentRepository.findCommentsByVideosIdAsync(any())).thenReturn(CompletableFuture.failedFuture(new Exception()));

        this.service.getVideoComments(grpcReq, responseObserver);
        verify(responseObserver, times(1)).onError(any());
        verify(responseObserver, times(0)).onNext(any());
        verify(responseObserver, times(0)).onCompleted();
    }

    @Test
    public void testGetVideoComments() {
        GetVideoCommentsRequest grpcReq = GetVideoCommentsRequest.getDefaultInstance();
        StreamObserver<GetVideoCommentsResponse> responseObserver = mock(StreamObserver.class);

        doNothing().when(validator).validateGrpcRequestGetVideoComment(any(),any());
        QueryCommentByVideo query = mock(QueryCommentByVideo.class);
        when(mapper.mapFromGrpcVideoCommentToDseQuery(any())).thenReturn(query);

        ResultListPage<Comment> resultListPage = mock(ResultListPage.class);
        when(commentRepository.findCommentsByVideosIdAsync(any())).thenReturn(CompletableFuture.completedFuture(resultListPage));

        this.service.getVideoComments(grpcReq, responseObserver);
        verify(responseObserver, times(0)).onError(any());
        verify(responseObserver, times(1)).onNext(any());
        verify(responseObserver, times(1)).onCompleted();
    }
}
