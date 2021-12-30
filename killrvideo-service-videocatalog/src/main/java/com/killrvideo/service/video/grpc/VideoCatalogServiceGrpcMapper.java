package com.killrvideo.service.video.grpc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.protobuf.Timestamp;
import com.killrvideo.dse.dto.CustomPagingState;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.video.dto.LatestVideo;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.dto.UserVideo;

import com.killrvideo.dse.dto.Video;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData;
import com.killrvideo.utils.GrpcMappingUtils;
import killrvideo.common.CommonTypes;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoLocationType;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoPreview;
import killrvideo.video_catalog.events.VideoCatalogEvents;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.killrvideo.utils.GrpcMappingUtils.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Utility mapping GRPC.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
public class VideoCatalogServiceGrpcMapper {

    public Video mapSubmitYouTubeVideoRequestAsVideo(SubmitYouTubeVideoRequest request) {
        Video targetVideo = new Video();
        targetVideo.setVideoid(fromUuid(request.getVideoId()));
        targetVideo.setUserid(fromUuid(request.getUserId()));
        targetVideo.setName(request.getName());
        targetVideo.setLocation(request.getYouTubeVideoId());
        targetVideo.setDescription(request.getDescription());
        targetVideo.setPreviewImageLocation("//img.youtube.com/vi/" + targetVideo.getLocation() + "/hqdefault.jpg");
        targetVideo.setTags(Sets.newHashSet(request.getTagsList().iterator()));
        targetVideo.setLocationType(VideoLocationType.YOUTUBE.ordinal());
        return targetVideo;
    }

    public GetLatestVideoPreviewsResponse mapLatestVideoToGrpcResponse(LatestVideosPage returnedPage) {
        return GetLatestVideoPreviewsResponse.newBuilder()
                .addAllVideoPreviews(
                        returnedPage.getListOfPreview().stream()
                                .map(this::mapLatestVideotoVideoPreview)
                                .collect(Collectors.toList()))
                .setPagingState(returnedPage.getNextPageState())
                .build();
    }

    /**
     * Mapping to GRPC generated classes.
     */
    private VideoPreview mapLatestVideotoVideoPreview(LatestVideo lv) {
        return VideoPreview.newBuilder()
                .setAddedDate(instantToTimeStamp(lv.getAddedDate()))
                .setName(lv.getName())
                .setPreviewImageLocation(Optional.ofNullable(lv.getPreviewImageLocation()).orElse("N/A"))
                .setUserId(uuidToUuid(lv.getUserid()))
                .setVideoId(uuidToUuid(lv.getVideoid()))
                .build();
    }

    /**
     * Mapping to generated GPRC beans.
     */
    private VideoPreview mapFromUserVideotoVideoPreview(UserVideo v) {
        return VideoPreview.newBuilder()
                .setAddedDate(instantToTimeStamp(v.getAddedDate()))
                .setName(v.getName())
                .setPreviewImageLocation(Optional.ofNullable(v.getPreviewImageLocation()).orElse("N/A"))
                .setUserId(uuidToUuid(v.getUserid()))
                .setVideoId(uuidToUuid(v.getVideoid()))
                .build();
    }

    /**
     * Mapping to generated GPRC beans (Full detailed)
     */
    public GetVideoResponse mapFromVideotoVideoResponse(Video v) {
        // Check to see if any tags exist, if not, ensure to send an empty set instead of null
        if (CollectionUtils.isEmpty(v.getTags())) {
            v.setTags(Collections.emptySet());
        }
        return GetVideoResponse.newBuilder()
                .setAddedDate(instantToTimeStamp(v.getAddedDate()))
                .setDescription(v.getDescription())
                .setLocation(v.getLocation())
                .setLocationType(VideoLocationType.forNumber(v.getLocationType()))
                .setName(v.getName())
                .setUserId(uuidToUuid(v.getUserid()))
                .setVideoId(uuidToUuid(v.getVideoid()))
                .addAllTags(v.getTags())
                .build();
    }

    public VideoCatalogEvents.YouTubeVideoAdded createYouTubeVideoAddedEvent(Video video) {
        return VideoCatalogEvents.YouTubeVideoAdded.newBuilder()
                .setAddedDate(Timestamp.newBuilder().build())
                .setDescription(video.getDescription())
                .setLocation(video.getLocation())
                .setName(video.getName())
                .setPreviewImageLocation(video.getPreviewImageLocation())
                .setUserId(uuidToUuid(video.getUserid()))
                .setVideoId(uuidToUuid(video.getVideoid()))
                .build();
    }

    public GetLatestVideoPreviewsRequestData parseGetLatestVideoPreviewsRequest(
            GetLatestVideoPreviewsRequest grpcReq,
            Supplier<CustomPagingState> firstCustomPagingStateFactory) {
        CustomPagingState pageState =
                CustomPagingState.deserialize(grpcReq.getPagingState())
                        .orElse(firstCustomPagingStateFactory.get());
        int pageSize = grpcReq.getPageSize();
        final Optional<Instant> startDate = Optional.of(grpcReq.getStartingAddedDate())
                .filter(x -> isNotBlank(x.toString()))
                .map(GrpcMappingUtils::timestampToInstant);
        final Optional<UUID> startVideoId = Optional.of(grpcReq.getStartingVideoId())
                .filter(x -> isNotBlank(x.toString()))
                .map(GrpcMappingUtils::fromUuid);

        return new GetLatestVideoPreviewsRequestData(pageState, pageSize, startDate, startVideoId);
    }

    public GetVideoPreviewsResponse mapToGetVideoPreviewsResponse(List<Video> videos) {
        final GetVideoPreviewsResponse.Builder builder = GetVideoPreviewsResponse.newBuilder();
        videos.stream()
                .map(this::mapFromVideotoVideoPreview)
                .forEach(builder::addVideoPreviews);
        return builder.build();
    }

    /**
     * Mapping to generated GPRC beans.
     */
    private VideoPreview mapFromVideotoVideoPreview(Video v) {
        return VideoPreview.newBuilder()
                .setAddedDate(instantToTimeStamp(v.getAddedDate()))
                .setName(v.getName())
                .setPreviewImageLocation(Optional.ofNullable(v.getPreviewImageLocation()).orElse("N/A"))
                .setUserId(uuidToUuid(v.getUserid()))
                .setVideoId(uuidToUuid(v.getVideoid()))
                .build();
    }

    public GetUserVideoPreviewsRequestData parseGetUserVideoPreviewsRequest(GetUserVideoPreviewsRequest grpcReq) {
        final UUID userId = fromUuid(grpcReq.getUserId());
        final Optional<UUID> startingVideoId = Optional
                .of(grpcReq.getStartingVideoId())
                .filter(uuid -> isNotBlank(uuid.getValue()))
                .map(GrpcMappingUtils::fromUuid);
        final Optional<Instant> startingAddedDate = Optional
                .of(grpcReq.getStartingAddedDate())
                .map(GrpcMappingUtils::timestampToInstant);
        final Optional<Integer> pagingSize =
                Optional.of(grpcReq.getPageSize());
        final Optional<String> pagingState =
                Optional.of(grpcReq.getPagingState())
                        .filter(StringUtils::isNotBlank);

        return new GetUserVideoPreviewsRequestData(
                userId, startingVideoId, startingAddedDate, pagingSize, pagingState
        );
    }

    public GetUserVideoPreviewsResponse mapToGetUserVideoPreviewsResponse(ResultListPage<UserVideo> resultPage, UUID userId) {
        CommonTypes.Uuid userGrpcUUID = GrpcMappingUtils.uuidToUuid(userId);
        final GetUserVideoPreviewsResponse.Builder builder = GetUserVideoPreviewsResponse.newBuilder().setUserId(userGrpcUUID);
        resultPage.getResults().stream()
                .map(this::mapFromUserVideotoVideoPreview)
                .forEach(builder::addVideoPreviews);
        resultPage.getPagingState().ifPresent(builder::setPagingState);
        return builder.build();
    }
}
