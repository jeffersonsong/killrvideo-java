package com.killrvideo.service.comment.grpc;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.comment.dto.Comment;
import com.killrvideo.service.comment.dto.QueryCommentByUser;
import killrvideo.comments.CommentsServiceOuterClass.*;
import killrvideo.common.CommonTypes.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToTimeUuid;
import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class CommentsServiceGrpcMapperTest {
    private CommentsServiceGrpcMapper mapper = new CommentsServiceGrpcMapper();

    @Test
    public void testMapFromGrpcUserCommentToDseQuery() {
        UUID userId = UUID.randomUUID();
        UUID startingCommentId = Uuids.timeBased();
        GetUserCommentsRequest request = GetUserCommentsRequest.newBuilder()
                .setStartingCommentId(uuidToTimeUuid(startingCommentId))
                .setUserId(uuidToUuid(userId))
                .setPageSize(5)
                .setPagingState("paging state")
                .build();

        QueryCommentByUser pojo = mapper.mapFromGrpcUserCommentToDseQuery(request);
        assertEquals(userId, pojo.getUserId());
        assertEquals(startingCommentId, pojo.getCommentId().get());
        assertEquals(5, pojo.getPageSize());
        assertTrue(pojo.getPageState().isPresent());
        assertEquals("paging state", pojo.getPageState().get());
    }

    @Test
    public void testMapFromGrpcUserCommentToDseQueryWithoutCommentId() {
        UUID userId = UUID.randomUUID();
        GetUserCommentsRequest request = GetUserCommentsRequest.newBuilder()
                .setUserId(uuidToUuid(userId))
                .setPageSize(5)
                .build();

        QueryCommentByUser pojo = mapper.mapFromGrpcUserCommentToDseQuery(request);
        assertEquals(userId, pojo.getUserId());
        assertFalse(pojo.getCommentId().isPresent());
        assertEquals(5, pojo.getPageSize());
        assertFalse(pojo.getPageState().isPresent());
    }

    @Test
    public void testMapFromDseVideoCommentToGrpcResponse() {
        Comment comment = new Comment();
        comment.setComment("test");
        comment.setCommentid(Uuids.timeBased());
        comment.setUserid(UUID.randomUUID());
        comment.setVideoid(UUID.randomUUID());
        comment.setDateOfComment(Instant.now());

        ResultListPage<Comment> comments = new ResultListPage<>(singletonList(comment), Optional.empty());

        GetVideoCommentsResponse proto = mapper.mapFromDseVideoCommentToGrpcResponse(comments);

        assertEquals(comment.getVideoid().toString(), proto.getVideoId().getValue());
    }

    @Test
    public void testMapFromDseUserCommentToGrpcResponse() {
    }

    @Test
    public void testMapFromGrpcVideoCommentToDseQuery() {
    }

    @Test
    public void testMapToComment() {
    }

    @Test
    public void testCreateUserCommentedOnVideoEvent() {
    }
}