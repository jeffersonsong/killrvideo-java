package com.killrvideo.service.comment.grpc;

import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.killrvideo.utils.GrpcMappingUtils.randomTimeUuid;
import static com.killrvideo.utils.GrpcMappingUtils.randomUuid;
import static org.mockito.Mockito.*;

class CommentsServiceGrpcValidatorTest {
    private final CommentsServiceGrpcValidator validator = new CommentsServiceGrpcValidator();
    private StreamObserver<?> streamObserver;

    @BeforeEach
    public void setUp() {
        this.streamObserver = mock(StreamObserver.class);
    }

    @Test
    public void testValidateGrpcRequestCommentOnVideoSuccess() {
        CommentOnVideoRequest request = CommentOnVideoRequest.newBuilder()
                .setUserId(randomUuid())
                .setVideoId(randomUuid())
                .setCommentId(randomTimeUuid())
                .setComment("Comment")
                .build();

        validator.validateGrpcRequestCommentOnVideo(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequestCommentOnVideoFailed() {
        CommentOnVideoRequest request = CommentOnVideoRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                        validator.validateGrpcRequestCommentOnVideo(request, streamObserver)
                );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequestGetVideoCommentSuccess() {
        GetVideoCommentsRequest request = GetVideoCommentsRequest.newBuilder()
                .setVideoId(randomUuid())
                .setPageSize(2)
                .build();

        validator.validateGrpcRequestGetVideoComment(request, streamObserver);
        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequestGetVideoCommentFailed() {
        GetVideoCommentsRequest request = GetVideoCommentsRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequestGetVideoComment(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_GetUserCommentsSuccess() {
        GetUserCommentsRequest request = GetUserCommentsRequest.newBuilder()
                .setUserId(randomUuid())
                .setPageSize(2)
                .build();

        validator.validateGrpcRequest_GetUserComments(request, streamObserver);
        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_GetUserCommentsFailed() {
        GetUserCommentsRequest request = GetUserCommentsRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_GetUserComments(request, streamObserver)
        );

        verifyFailure();
    }

    private void verifySuccess() {
        verify(streamObserver, times(0)).onError(any());
        verify(streamObserver, times(0)).onCompleted();
    }

    private void verifyFailure() {
        verify(streamObserver, times(1)).onError(any());
        verify(streamObserver, times(1)).onCompleted();
    }
}