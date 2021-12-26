package com.killrvideo.service.video.grpc;

import com.killrvideo.dse.dto.CustomPagingState;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.video.repository.VideoCatalogRepository;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.video_catalog.VideoCatalogServiceGrpc.VideoCatalogServiceImplBase;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.killrvideo.utils.GrpcUtils.returnSingleResult;
import static java.util.stream.Collectors.toList;

/**
 * Exposition of comment services with GPRC Technology & Protobuf Interface
 *
 * @author DataStax Developer Advocates team.
 */
@Service
public class VideoCatalogServiceGrpc extends VideoCatalogServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoCatalogServiceGrpc.class);

    /**
     * Send new videos.
     */
    @Value("${killrvideo.messaging.destinations.youTubeVideoAdded : topic-kv-videoCreation}")
    private String topicVideoCreated;

    @Value("${killrvideo.discovery.services.videoCatalog : VideoCatalogService}")
    private String serviceKey;

    private final VideoCatalogRepository videoCatalogRepository;
    private final MessagingDao messagingDao;
    private final VideoCatalogServiceGrpcValidator validator;
    private final VideoCatalogServiceGrpcMapper mapper;

    public VideoCatalogServiceGrpc(VideoCatalogRepository videoCatalogRepository, MessagingDao messagingDao, VideoCatalogServiceGrpcValidator validator, VideoCatalogServiceGrpcMapper mapper) {
        this.videoCatalogRepository = videoCatalogRepository;
        this.messagingDao = messagingDao;
        this.validator = validator;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submitYouTubeVideo(SubmitYouTubeVideoRequest grpcReq, StreamObserver<SubmitYouTubeVideoResponse> grpcResObserver) {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_submitYoutubeVideo(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // Mapping GRPC => Domain (Dao)
        Video video = mapper.mapSubmitYouTubeVideoRequestAsVideo(grpcReq);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Insert youtube video {} for user {} : {}", video.getVideoid(), video.getUserid(), video);
        }

        // Execute query (ASYNC)
        videoCatalogRepository.insertVideoAsync(video)
                .thenCompose(rs ->
                        // If OK, then send Message to Kafka
                        messagingDao.sendEvent(topicVideoCreated, mapper.createYouTubeVideoAddedEvent(video))
                )
                .whenComplete((result, error) -> {
                    // Building Response
                    if (error != null) {
                        traceError("submitYouTubeVideo", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                    } else {
                        traceSuccess("submitYouTubeVideo", starts);
                        returnSingleResult(SubmitYouTubeVideoResponse.newBuilder().build(), grpcResObserver);
                    }
                });
    }

    /**
     * Get the latest video (Home Page)
     * <p>
     * In this method, we craft our own paging state. The custom paging state format is:
     * <br/>
     * <br/>
     * <code>
     * yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd,&lt;index&gt;,&lt;Cassandra paging state as string&gt;
     * </code>
     * <br/>
     * <br/>
     * <ul>
     *     <li>The first field is the date of 7 days in the past, starting from <strong>now</strong></li>
     *     <li>The second field is the index in this date list, to know at which day in the past we stop at the previous query</li>
     *     <li>The last field is the serialized form of the native Cassandra paging state</li>
     * </ul>
     * <p>
     * On the first query, we create our own custom paging state in the server by computing the list of 8 days
     * in the past, the <strong>index</strong> is set to 0 and there is no native Cassandra paging state
     * <p>
     * <br/>
     * On subsequent request, we decode the custom paging state coming from the web app and resume querying from
     * the appropriate date, and we inject also the native Cassandra paging state.
     * <br/>
     * <strong>However, we can only use the native Cassandra paging state for the 1st query in the for loop. Indeed
     * Cassandra paging state is a hash of query string and bound values. We may switch partition to move one day
     * back in the past to fetch more results so the paging state will no longer be usable</strong>]
     */
    @Override
    public void getLatestVideoPreviews(GetLatestVideoPreviewsRequest grpcReq, StreamObserver<GetLatestVideoPreviewsResponse> grpcResObserver) {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_getLatestPreviews(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // GRPC Parameters Mappings
        GetLatestVideoPreviewsRequestData requestData = mapper.parseGetLatestVideoPreviewsRequest(
                grpcReq, videoCatalogRepository::buildFirstCustomPagingState
        );

        videoCatalogRepository.getLatestVideoPreviewsAsync(requestData)
                .whenComplete((returnedPage, error) -> {
                    if (error != null) {
                        traceError("getLatestVideoPreviews", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                    } else {
                        traceSuccess("getLatestVideoPreviews", starts);
                        returnSingleResult(mapper.mapLatestVideoToGrpcResponse(returnedPage), grpcResObserver);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getVideo(GetVideoRequest grpcReq, StreamObserver<GetVideoResponse> grpcResObserver) {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_getVideo(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // GRPC Parameters Mappings
        final UUID videoId = UUID.fromString(grpcReq.getVideoId().getValue());

        // Invoke Async
        videoCatalogRepository.getVideoById(videoId)
                .whenComplete((video, error) -> {
                    // Map back as GRPC (if correct invalid credential otherwize)
                    if (error != null) {
                        traceError("getVideo", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                    } else if (video == null) {
                        LOGGER.warn("Video with id " + videoId + " was not found");
                        traceError("getVideo", starts, error);
                        grpcResObserver.onError(Status.NOT_FOUND.withDescription("Video with id " + videoId + " was not found").asRuntimeException());
                    } else {
                        traceSuccess("getVideo", starts);
                        GetVideoResponse response = mapper.mapFromVideotoVideoResponse(video);
                        returnSingleResult(response, grpcResObserver);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getVideoPreviews(GetVideoPreviewsRequest grpcReq, StreamObserver<GetVideoPreviewsResponse> grpcResObserver) {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_getVideoPreviews(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        if (grpcReq.getVideoIdsCount() == 0) {
            traceSuccess("getVideoPreviews", starts);
            returnSingleResult(GetVideoPreviewsResponse.getDefaultInstance(), grpcResObserver);
            LOGGER.warn("No video id provided for video preview");
        } else {
            // GRPC Parameters Mappings
            List<UUID> listOfVideoIds = grpcReq.getVideoIdsList().stream().map(Uuid::getValue).map(UUID::fromString).collect(toList());

            // Execute Async
            videoCatalogRepository.getVideoPreview(listOfVideoIds)
                    .whenComplete((videos, error) -> {
                        // Mapping back as GRPC
                        if (error != null) {
                            traceError("getVideoPreviews", starts, error);
                            grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                        } else {
                            traceSuccess("getVideoPreviews", starts);
                            GetVideoPreviewsResponse response = mapper.mapToGetVideoPreviewsResponse(videos);
                            returnSingleResult(response, grpcResObserver);
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getUserVideoPreviews(GetUserVideoPreviewsRequest grpcReq, StreamObserver<GetUserVideoPreviewsResponse> grpcResObserver) {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_getUserVideoPreviews(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // GRPC Parameters Mappings
        GetUserVideoPreviewsRequestData requestData = mapper.parseGetUserVideoPreviewsRequest(grpcReq);
        // Map Result back to GRPC
        videoCatalogRepository.getUserVideosPreview(requestData)
                .whenComplete((resultPage, error) -> {
                    if (error != null) {
                        traceError("getUserVideoPreviews", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());

                    } else {
                        traceSuccess("getUserVideoPreviews", starts);
                        GetUserVideoPreviewsResponse response =
                                mapper.mapToGetUserVideoPreviewsResponse(resultPage, requestData.getUserId());
                        returnSingleResult(response, grpcResObserver);
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
