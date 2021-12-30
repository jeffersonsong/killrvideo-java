package com.killrvideo.service.comment.grpc;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.comment.dto.Comment;
import com.killrvideo.service.comment.dto.QueryCommentByUser;
import com.killrvideo.service.comment.dto.QueryCommentByVideo;
import killrvideo.comments.CommentsServiceOuterClass.*;
import killrvideo.comments.events.CommentsEvents;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToTimeUuid;
import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class CommentsServiceGrpcMapperTest {
    private final CommentsServiceGrpcMapper mapper = new CommentsServiceGrpcMapper();

    @Test
    public void testMapFromGrpcUserCommentToDseQuery() {
        UUID userId = UUID.randomUUID();
        UUID startingCommentId = Uuids.timeBased();
        GetUserCommentsRequest request = getUserCommentsRequestWithStartingCommentIdAndState(userId, startingCommentId);

        QueryCommentByUser pojo = mapper.mapFromGrpcUserCommentToDseQuery(request);
        assertEquals(userId, pojo.getUserId());
        assertTrue(pojo.getCommentId().isPresent());
        assertEquals(startingCommentId, pojo.getCommentId().get());
        assertEquals(5, pojo.getPageSize());
        assertTrue(pojo.getPageState().isPresent());
        assertEquals("paging state", pojo.getPageState().get());
    }

    @Test
    public void testMapFromGrpcUserCommentToDseQueryWithoutCommentId() {
        UUID userId = UUID.randomUUID();
        GetUserCommentsRequest request = getUserCommentsRequestInitial(userId);

        QueryCommentByUser pojo = mapper.mapFromGrpcUserCommentToDseQuery(request);
        assertEquals(userId, pojo.getUserId());
        assertFalse(pojo.getCommentId().isPresent());
        assertEquals(5, pojo.getPageSize());
        assertFalse(pojo.getPageState().isPresent());
    }

    @Test
    public void testMapFromDseVideoCommentToGrpcResponse() {
        Comment comment = comment();
        ResultListPage<Comment> comments = new ResultListPage<>(singletonList(comment), Optional.empty());
        GetVideoCommentsResponse proto = mapper.mapFromDseVideoCommentToGrpcResponse(comments);
        assertEquals(comment.getVideoid().toString(), proto.getVideoId().getValue());
        assertEquals(1, proto.getCommentsCount());
    }

    @Test
    public void testMapFromDseUserCommentToGrpcResponse() {
        Comment comment = comment();
        ResultListPage<Comment> comments = new ResultListPage<>(singletonList(comment), Optional.empty());
        GetUserCommentsResponse proto = mapper.mapFromDseUserCommentToGrpcResponse(comments);
        assertEquals(comment.getUserid().toString(), proto.getUserId().getValue());
        assertEquals(1, proto.getCommentsCount());
    }

    @Test
    public void testMapFromGrpcVideoCommentToDseQuery() {
        UUID videoid = UUID.randomUUID();
        UUID startingCommentId = Uuids.timeBased();
        GetVideoCommentsRequest request = getVideoCommentsRequestWithStartingCommentIdAndState(videoid, startingCommentId);

        QueryCommentByVideo pojo = mapper.mapFromGrpcVideoCommentToDseQuery(request);
        assertEquals(videoid, pojo.getVideoId());
        assertTrue(pojo.getCommentId().isPresent());
        assertEquals(startingCommentId, pojo.getCommentId().get());
        assertEquals(5, pojo.getPageSize());
        assertTrue(pojo.getPageState().isPresent());
        assertEquals("paging state", pojo.getPageState().get());
    }

    @Test
    public void testMapFromGrpcVideoCommentToDseQueryWithoutCommentId() {
        UUID videoid = UUID.randomUUID();
        GetVideoCommentsRequest request = getVideoCommentsRequestInitial(videoid);

        QueryCommentByVideo pojo = mapper.mapFromGrpcVideoCommentToDseQuery(request);
        assertEquals(videoid, pojo.getVideoId());
        assertEquals(5, pojo.getPageSize());
        assertFalse(pojo.getPageState().isPresent());
    }

    @Test
    public void testMapToComment() {
        UUID vidoid = UUID.randomUUID();
        UUID userid = UUID.randomUUID();
        UUID commentid = Uuids.timeBased();
        String commentText = "Test";
        CommentOnVideoRequest request = commentOnVideoRequest(vidoid, userid, commentid, commentText);

        Comment pojo = mapper.mapToComment(request);
        assertEquals(vidoid, pojo.getVideoid());
        assertEquals(userid, pojo.getUserid());
        assertEquals(commentid, pojo.getCommentid());
        assertEquals(commentText, pojo.getComment());
    }

    @Test
    public void testCreateUserCommentedOnVideoEvent() {
        Comment comment = comment();
        CommentsEvents.UserCommentedOnVideo event = mapper.createUserCommentedOnVideoEvent(comment);
        assertEquals(comment.getCommentid().toString(), event.getCommentId().getValue());
        assertEquals(comment.getVideoid().toString(), event.getVideoId().getValue());
        assertEquals(comment.getUserid().toString(), event.getUserId().getValue());
        assertNotNull(event.getCommentTimestamp());
    }

    private GetUserCommentsRequest getUserCommentsRequestInitial(UUID userId) {
        return GetUserCommentsRequest.newBuilder()
                .setUserId(uuidToUuid(userId))
                .setPageSize(5)
                .build();
    }

    private GetUserCommentsRequest getUserCommentsRequestWithStartingCommentIdAndState(UUID userId, UUID startingCommentId) {
        return GetUserCommentsRequest.newBuilder()
                .setStartingCommentId(uuidToTimeUuid(startingCommentId))
                .setUserId(uuidToUuid(userId))
                .setPageSize(5)
                .setPagingState("paging state")
                .build();
    }

    private GetVideoCommentsRequest getVideoCommentsRequestWithStartingCommentIdAndState(UUID videoid, UUID startingCommentId) {
        return GetVideoCommentsRequest.newBuilder()
                .setStartingCommentId(uuidToTimeUuid(startingCommentId))
                .setVideoId(uuidToUuid(videoid))
                .setPageSize(5)
                .setPagingState("paging state")
                .build();
    }

    private GetVideoCommentsRequest getVideoCommentsRequestInitial(UUID videoid) {
        return GetVideoCommentsRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .setPageSize(5)
                .build();
    }

    private CommentOnVideoRequest commentOnVideoRequest(UUID vidoid, UUID userid, UUID commentid, String commentText) {
        return CommentOnVideoRequest.newBuilder()
                .setVideoId(uuidToUuid(vidoid))
                .setCommentId(uuidToTimeUuid(commentid))
                .setUserId(uuidToUuid(userid))
                .setComment(commentText)
                .build();
    }

    private Comment comment() {
        Comment comment = new Comment();
        comment.setComment("test");
        comment.setCommentid(Uuids.timeBased());
        comment.setUserid(UUID.randomUUID());
        comment.setVideoid(UUID.randomUUID());
        comment.setDateOfComment(Instant.now());
        return comment;
    }
}