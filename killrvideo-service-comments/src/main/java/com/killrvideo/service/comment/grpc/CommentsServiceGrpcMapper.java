package com.killrvideo.service.comment.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.comment.dto.Comment;
import com.killrvideo.service.comment.dto.QueryCommentByUser;
import com.killrvideo.service.comment.dto.QueryCommentByVideo;
import com.killrvideo.utils.GrpcMappingUtils;
import killrvideo.comments.CommentsServiceOuterClass;
import killrvideo.comments.CommentsServiceOuterClass.*;
import killrvideo.comments.events.CommentsEvents;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

import static com.killrvideo.utils.GrpcMappingUtils.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Validation of inputs and mapping
 *
 * @author DataStax Developer Advocates team.
 */
@Component
public class CommentsServiceGrpcMapper {
    // --- Mappings ---
    
    /**
     * Utility from exposition to Dse query.
     * 
     * @param grpcReq
     *      grpc Request
     * @return
     *      query bean for Dao
     */
    public QueryCommentByUser mapFromGrpcUserCommentToDseQuery(GetUserCommentsRequest grpcReq) {
        QueryCommentByUser targetQuery = new QueryCommentByUser();
        if (grpcReq.hasStartingCommentId() && 
                !isBlank(grpcReq.getStartingCommentId().getValue())) {
            targetQuery.setCommentId(Optional.of(fromTimeUuid(grpcReq.getStartingCommentId())));
        }
        targetQuery.setUserId(fromUuid(grpcReq.getUserId()));
        targetQuery.setPageSize(grpcReq.getPageSize());
        if (isNotBlank(grpcReq.getPagingState())) {
            targetQuery.setPageState(Optional.of(grpcReq.getPagingState()));
        }
        return targetQuery;
    }
    
    // Map from CommentDseDao response bean to expected GRPC object.
    public GetVideoCommentsResponse mapFromDseVideoCommentToGrpcResponse(ResultListPage<Comment> comments) {
        final GetVideoCommentsResponse.Builder builder = GetVideoCommentsResponse.newBuilder();
        for (Comment c : comments.getResults()) {
           builder.setVideoId(uuidToUuid(c.getVideoid()));
           builder.addComments(newVideoCommentProto(c));
        }
        comments.getPagingState().ifPresent(builder::setPagingState);
        return builder.build();
    }

    private CommentsServiceOuterClass.VideoComment newVideoCommentProto(Comment c) {
        return CommentsServiceOuterClass.VideoComment.newBuilder()
                .setComment(c.getComment())
                .setUserId(uuidToUuid(c.getUserid()))
                .setCommentId(uuidToTimeUuid(c.getCommentid()))
                .setCommentTimestamp(instantToTimeStamp(c.getDateOfComment()))
                .build();
    }

    // Map from CommentDseDao response bean to expected GRPC object.
    public GetUserCommentsResponse mapFromDseUserCommentToGrpcResponse(ResultListPage<Comment> dseRes) {
        final GetUserCommentsResponse.Builder builder = GetUserCommentsResponse.newBuilder();
        for (Comment c : dseRes.getResults()) {
           builder.setUserId(uuidToUuid(c.getUserid()));
           builder.addComments(newUserCommentProto(c));
        }
        dseRes.getPagingState().ifPresent(builder::setPagingState);
        return builder.build();
    }

    private CommentsServiceOuterClass.UserComment newUserCommentProto(Comment c) {
        return CommentsServiceOuterClass.UserComment.newBuilder()
                .setComment(c.getComment())
                .setCommentId(uuidToTimeUuid(c.getCommentid()))
                .setVideoId(uuidToUuid(c.getVideoid()))
                .setCommentTimestamp(instantToTimeStamp(c.getDateOfComment()))
                .build();
    }

    /**
     * Utility from exposition to Dse query.
     * 
     * @param grpcReq
     *      grpc Request
     * @return
     *      query bean for Dao
     */
    public QueryCommentByVideo mapFromGrpcVideoCommentToDseQuery(GetVideoCommentsRequest grpcReq) {
        QueryCommentByVideo targetQuery = new QueryCommentByVideo();
        if (grpcReq.hasStartingCommentId() && 
                !isBlank(grpcReq.getStartingCommentId().getValue())) {
            targetQuery.setCommentId(Optional.of(fromTimeUuid(grpcReq.getStartingCommentId())));
        }
        targetQuery.setVideoId(fromUuid(grpcReq.getVideoId()));
        targetQuery.setPageSize(grpcReq.getPageSize());
        if (isNotBlank(grpcReq.getPagingState())) {
            targetQuery.setPageState(Optional.of(grpcReq.getPagingState()));
        }
        return targetQuery;
    }

    public Comment mapToComment(CommentOnVideoRequest grpcReq) {
        Comment comment = new Comment();
        comment.setVideoid(fromUuid(grpcReq.getVideoId()));
        comment.setCommentid(fromTimeUuid(grpcReq.getCommentId()));
        comment.setUserid(fromUuid(grpcReq.getUserId()));
        comment.setComment(grpcReq.getComment());
        return comment;
    }

    public CommentsEvents.UserCommentedOnVideo createUserCommentedOnVideoEvent(Comment comment) {
        return CommentsEvents.UserCommentedOnVideo.newBuilder()
                .setCommentId(uuidToTimeUuid(comment.getCommentid()))
                .setVideoId(uuidToUuid(comment.getVideoid()))
                .setUserId(uuidToUuid(comment.getUserid()))
                .setCommentTimestamp(GrpcMappingUtils.instantToTimeStamp(Instant.now()))
                .build();
    }
}
