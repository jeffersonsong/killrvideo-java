package com.killrvideo.service.video.grpc;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.killrvideo.service.video.dto.LatestVideo;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.dto.UserVideo;

import com.killrvideo.dse.dto.Video;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoLocationType;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoPreview;
import org.springframework.stereotype.Component;

import static com.killrvideo.utils.GrpcMappingUtils.*;

/**
 * Utility mapping GRPC.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
public class VideoCatalogServiceGrpcMapper {

    public Video mapSubmitYouTubeVideoRequestAsVideo(SubmitYouTubeVideoRequest request) {
        Video targetVideo = new Video();
        targetVideo.setVideoid(UUID.fromString(request.getVideoId().getValue()));
        targetVideo.setUserid(UUID.fromString(request.getUserId().getValue()));
        targetVideo.setName(request.getName());
        targetVideo.setLocation(request.getYouTubeVideoId());
        targetVideo.setDescription(request.getDescription());
        targetVideo.setPreviewImageLocation("//img.youtube.com/vi/"+ targetVideo.getLocation() + "/hqdefault.jpg");
        targetVideo.setTags(Sets.newHashSet(request.getTagsList().iterator()));
        targetVideo.setLocationType(VideoLocationType.YOUTUBE.ordinal());
        return targetVideo;
    }
    
    /**
     * Mapping to GRPC generated classes.
     */
    public VideoPreview mapLatestVideotoVideoPreview(LatestVideo lv) {
        return VideoPreview.newBuilder()
                .setAddedDate(instantToTimeStamp(lv.getAddedDate()))
                .setName(lv.getName())
                .setPreviewImageLocation(Optional.ofNullable(lv.getPreviewImageLocation()).orElse("N/A"))
                .setUserId(uuidToUuid(lv.getUserid()))
                .setVideoId(uuidToUuid(lv.getVideoid()))
                .build();
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
     * Mapping to generated GPRC beans.
     */
    public VideoPreview mapFromVideotoVideoPreview(Video v) {
        return VideoPreview.newBuilder()
                .setAddedDate(instantToTimeStamp(v.getAddedDate()))
                .setName(v.getName())
                .setPreviewImageLocation(Optional.ofNullable(v.getPreviewImageLocation()).orElse("N/A"))
                .setUserId(uuidToUuid(v.getUserid()))
                .setVideoId(uuidToUuid(v.getVideoid()))
                .build();
    }
    
    /**
     * Mapping to generated GPRC beans.
     */
    public VideoPreview mapFromUserVideotoVideoPreview(UserVideo v) {
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
}
