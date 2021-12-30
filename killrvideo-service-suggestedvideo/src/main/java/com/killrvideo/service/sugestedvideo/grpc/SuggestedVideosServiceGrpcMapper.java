package com.killrvideo.service.sugestedvideo.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.service.sugestedvideo.request.GetRelatedVideosRequestData;
import com.killrvideo.utils.GrpcMappingUtils;
import killrvideo.common.CommonTypes;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse;
import killrvideo.suggested_videos.SuggestedVideosService.SuggestedVideoPreview;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.*;

/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
public class SuggestedVideosServiceGrpcMapper {

    public Video mapVideoAddedtoVideoDTO(YouTubeVideoAdded videoAdded) {
        // Convert Stub to Dto, dao must not be related to interface GRPC
        Video video = new Video();
        video.setVideoid(fromUuid(videoAdded.getVideoId()));
        video.setAddedDate(timestampToInstant(videoAdded.getAddedDate()));
        video.setUserid(fromUuid(videoAdded.getUserId()));
        video.setName(videoAdded.getName());
        video.setTags(new HashSet<>(videoAdded.getTagsList()));
        video.setPreviewImageLocation(videoAdded.getPreviewImageLocation());
        video.setLocation(videoAdded.getLocation());
        return video;
    }

    /**
     * Mapping to generated GPRC beans. (Suggested videos special)
     */
    public SuggestedVideoPreview mapVideotoSuggestedVideoPreview(Video v) {
        return SuggestedVideoPreview.newBuilder()
                .setName(v.getName())
                .setVideoId(uuidToUuid(v.getVideoid()))
                .setUserId(uuidToUuid(v.getUserid()))
                .setPreviewImageLocation(v.getPreviewImageLocation())
                .setAddedDate(GrpcMappingUtils.instantToTimeStamp(v.getAddedDate()))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    public GetRelatedVideosRequestData parseGetRelatedVideosRequestData(GetRelatedVideosRequest grpcReq) {
        final UUID videoId = fromUuid(grpcReq.getVideoId());
        int videoPageSize = grpcReq.getPageSize();
        Optional<String> videoPagingState = Optional.ofNullable(grpcReq.getPagingState()).filter(StringUtils::isNotBlank);

        return new GetRelatedVideosRequestData(videoId, videoPageSize, videoPagingState);
    }

    public GetRelatedVideosResponse mapToGetRelatedVideosResponse(ResultListPage<Video> resultPage, UUID videoId) {
        CommonTypes.Uuid videoGrpcUUID = uuidToUuid(videoId);
        final GetRelatedVideosResponse.Builder builder =
                GetRelatedVideosResponse.newBuilder().setVideoId(videoGrpcUUID);
        resultPage.getResults().stream()
                .map(this::mapVideotoSuggestedVideoPreview)
                .filter(preview -> !preview.getVideoId().equals(videoGrpcUUID))
                .forEach(builder::addVideos);
        resultPage.getPagingState().ifPresent(builder::setPagingState);
        return builder.build();
    }
}
