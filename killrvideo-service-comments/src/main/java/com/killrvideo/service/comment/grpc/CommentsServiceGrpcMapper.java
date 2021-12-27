package com.killrvideo.service.comment.grpc;

import static com.killrvideo.service.comment.grpc.CommentsServiceGrpcValidator.initErrorString;
import static com.killrvideo.service.comment.grpc.CommentsServiceGrpcValidator.notEmpty;
import static com.killrvideo.service.comment.grpc.CommentsServiceGrpcValidator.positive;
import static com.killrvideo.service.comment.grpc.CommentsServiceGrpcValidator.validate;
import static com.killrvideo.utils.GrpcMappingUtils.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.killrvideo.utils.GrpcMappingUtils;
import killrvideo.comments.events.CommentsEvents;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.comment.dto.Comment;
import com.killrvideo.service.comment.dto.QueryCommentByUser;
import com.killrvideo.service.comment.dto.QueryCommentByVideo;

import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceOuterClass;
import killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse;
import killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse;

/**
 * Validation of inputs and mapping
 *
 * @author DataStax Developer Advocates team.
 */
@Component
public class CommentsServiceGrpcMapper {
    /**
     * Validate comment On video comment query.
     * 
     * @param request
     *      current GRPC Request
     * @param streamObserver
     *      response async
     */
    public void validateGrpcRequestCommentOnVideo(Logger logger, CommentOnVideoRequest request, StreamObserver<?> streamObserver) {
        StringBuilder errorMessage = initErrorString(request);
        boolean isValid = 
                  notEmpty(!request.hasUserId()    || isBlank(request.getUserId().getValue()),  "userId",  "video request",errorMessage) &&
                  notEmpty(!request.hasVideoId()   || isBlank(request.getVideoId().getValue()), "videoId", "video request",errorMessage) &&
                  notEmpty(!request.hasCommentId() || isBlank(request.getCommentId().getValue()), "commentId", "video request",errorMessage) &&
                  notEmpty(isBlank(request.getComment()), "comment", "video request",errorMessage);
        Assert.isTrue(validate(logger, streamObserver, errorMessage, isValid), "Invalid parameter for 'commentOnVideo'");
    }
    
    /**
     * Validate get video comment query.
     * 
     * @param request
     *      current GRPC Request
     * @param streamObserver
     *      response async
     */
    public void validateGrpcRequestGetVideoComment(Logger logger, GetVideoCommentsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (!request.hasVideoId() || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for get video comment request\n");
            isValid = false;
        }

        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for get video comment request");
            isValid = false;
        }

        Assert.isTrue(validate(logger, streamObserver, errorMessage, isValid), "Invalid parameter for 'getVideoComments'");
    }
    
    /**
     * Validate get user comment query.
     * 
     * @param request
     *      current GRPC Request
     * @param streamObserver
     *      response async
     */
    public void validateGrpcRequest_GetUserComments(Logger logger, GetUserCommentsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = 
                  notEmpty(!request.hasUserId() || isBlank(request.getUserId().getValue()),  "userId",  "comment request",errorMessage) &&
                  positive(request.getPageSize() <= 0,  "page size",  "comment request",errorMessage);
        Assert.isTrue(validate(logger, streamObserver, errorMessage, isValid), "Invalid parameter for 'getUserComments'");
    }
    
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
            targetQuery.setCommentId(Optional.of(UUID.fromString(grpcReq.getStartingCommentId().getValue())));
        }
        targetQuery.setUserId(UUID.fromString(grpcReq.getUserId().getValue()));
        targetQuery.setPageSize(grpcReq.getPageSize());
        targetQuery.setPageState(Optional.ofNullable(grpcReq.getPagingState()));
        return targetQuery;
    }
    
    // Map from CommentDseDao response bean to expected GRPC object.
    public GetVideoCommentsResponse mapFromDseVideoCommentToGrpcResponse(ResultListPage<Comment> dseRes) {
        final GetVideoCommentsResponse.Builder builder = GetVideoCommentsResponse.newBuilder();
        for (Comment c : dseRes.getResults()) {
           builder.setVideoId(uuidToUuid(c.getVideoid()));
           builder.addComments(newVideoCommentProto(c));
        }
        dseRes.getPagingState().ifPresent(builder::setPagingState);
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
            targetQuery.setCommentId(Optional.of(UUID.fromString(grpcReq.getStartingCommentId().getValue())));
        }
        targetQuery.setVideoId(UUID.fromString(grpcReq.getVideoId().getValue()));
        targetQuery.setPageSize(grpcReq.getPageSize());
        targetQuery.setPageState(Optional.ofNullable(grpcReq.getPagingState()));
        return targetQuery;
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
