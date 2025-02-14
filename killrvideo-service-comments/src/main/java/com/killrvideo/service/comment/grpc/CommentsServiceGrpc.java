package com.killrvideo.service.comment.grpc;

import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.comment.dto.Comment;
import com.killrvideo.service.comment.dto.QueryCommentByUser;
import com.killrvideo.service.comment.dto.QueryCommentByVideo;
import com.killrvideo.service.comment.repository.CommentRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceGrpc.CommentsServiceImplBase;
import killrvideo.comments.CommentsServiceOuterClass.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import static com.killrvideo.utils.GrpcUtils.returnSingleResult;

/**
 * Exposition of comment services with GPRC Technology & Protobuf Interface
 *
 * @author DataStax advocates team.
 */
@Service
public class CommentsServiceGrpc extends CommentsServiceImplBase {
    /**
     * Loger for that class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentsServiceGrpc.class);

    @Value("${killrvideo.discovery.services.comment : CommentsService}")
    private String serviceKey;

    @Value("${killrvideo.messaging.destinations.commentCreated : topic-kv-commentCreation}")
    private String messageDestination;

    /**
     * Communications and queries to DSE (Comment).
     */
    private final CommentRepository commentRepository;
    private final MessagingDao messagingDao;
    private final CommentsServiceGrpcValidator validator;
    private final CommentsServiceGrpcMapper mapper;

    public CommentsServiceGrpc(CommentRepository commentRepository, MessagingDao messagingDao, CommentsServiceGrpcValidator validator, CommentsServiceGrpcMapper mapper) {
        this.commentRepository = commentRepository;
        this.messagingDao = messagingDao;
        this.validator = validator;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commentOnVideo(final CommentOnVideoRequest grpcReq, StreamObserver<CommentOnVideoResponse> grpcResObserver) {
        // Boilerplate Code for validation delegated to {@link CommentsServiceGrpcValidator}
        validator.validateGrpcRequestCommentOnVideo(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao)
        Comment comment = mapper.mapToComment(grpcReq);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Insert comment on video {} for user {} : {}", comment.getVideoid(), comment.getUserid(), comment);
        }

        commentRepository.insertCommentAsync(comment)
                .thenCompose(rs ->
                        // If OK, then send Message to Kafka
                        messagingDao.sendEvent(messageDestination, mapper.createUserCommentedOnVideoEvent(rs))
                )
                .whenComplete((result, error) -> {
                    if (error != null) {
                        traceError("commentOnVideo", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                    } else {
                        traceSuccess("commentOnVideo", starts);
                        returnSingleResult(CommentOnVideoResponse.newBuilder().build(), grpcResObserver);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getVideoComments(final GetVideoCommentsRequest grpcReq, StreamObserver<GetVideoCommentsResponse> responseObserver) {
        // Parameter validations
        validator.validateGrpcRequestGetVideoComment(grpcReq, responseObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao) : Dedicated bean creating for flexibility
        QueryCommentByVideo query = mapper.mapFromGrpcVideoCommentToDseQuery(grpcReq);

        // ASYNCHRONOUS works with ComputableFuture
        commentRepository.findCommentsByVideosIdAsync(query).whenComplete((result, error) -> {
            if (error != null) {
                traceError("getVideoComments", starts, error);
                messagingDao.sendErrorEvent(getServiceKey(), error);
                responseObserver.onError(error);
            } else if (result != null) {
                traceSuccess("getVideoComments", starts);
                returnSingleResult(mapper.mapFromDseVideoCommentToGrpcResponse(result), responseObserver);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getUserComments(final GetUserCommentsRequest grpcReq, StreamObserver<GetUserCommentsResponse> responseObserver) {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_GetUserComments(grpcReq, responseObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao) : Dedicated bean creating for flexibility
        QueryCommentByUser query = mapper.mapFromGrpcUserCommentToDseQuery(grpcReq);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Listing comment for user {}", query.getUserId());
        }

        // ASYNCHRONOUS works with ComputableFuture
        commentRepository.findCommentsByUserIdAsync(query).whenComplete((result, error) -> {
            if (error != null) {
                traceError("getUserComments", starts, error);
                messagingDao.sendErrorEvent(getServiceKey(), error);
                responseObserver.onError(error);
            } else if (result != null) {
                traceSuccess("getUserComments", starts);
                returnSingleResult(mapper.mapFromDseUserCommentToGrpcResponse(result), responseObserver);
            }
        });
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    private void traceSuccess(String method, Instant starts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("End successfully '{}' in {} millis", method, Duration.between(starts, Instant.now()).getNano() / 1000);
        }
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    private void traceError(String method, Instant starts, Throwable t) {
        LOGGER.error("An error occured in {} after {}", method, Duration.between(starts, Instant.now()), t);
    }

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return current value of 'serviceKey'
     */
    public String getServiceKey() {
        return serviceKey;
    }
}
