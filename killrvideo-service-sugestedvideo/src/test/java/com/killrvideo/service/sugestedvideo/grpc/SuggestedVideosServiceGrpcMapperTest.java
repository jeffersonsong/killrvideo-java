package com.killrvideo.service.sugestedvideo.grpc;

import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.service.sugestedvideo.request.GetRelatedVideosRequestData;
import killrvideo.suggested_videos.SuggestedVideosService.*;
import killrvideo.video_catalog.events.VideoCatalogEvents.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class SuggestedVideosServiceGrpcMapperTest {
    private final SuggestedVideosServiceGrpcMapper mapper = new SuggestedVideosServiceGrpcMapper();

    @Test
    public void testMapVideoAddedtoVideoDTO() {
        Video v = video();

        YouTubeVideoAdded videoAdded = YouTubeVideoAdded.newBuilder()
                .setVideoId(uuidToUuid(v.getVideoid()))
                .setAddedDate(instantToTimeStamp(v.getAddedDate()))
                .setUserId(uuidToUuid(v.getUserid()))
                .setName(v.getName())
                .addAllTags(v.getTags())
                .setPreviewImageLocation(v.getPreviewImageLocation())
                .setLocation(v.getLocation())
                .build();

        Video video = mapper.mapVideoAddedtoVideoDTO(videoAdded);
        assertEquals(v.getVideoid(), video.getVideoid());
        assertEquals(v.getUserid(), video.getUserid());
        assertEquals(v.getAddedDate(), video.getAddedDate());
        assertEquals(v.getName(), video.getName());
        assertEquals(v.getTags().size(), video.getTags().size());
        assertEquals(v.getPreviewImageLocation(), video.getPreviewImageLocation());
        assertEquals(v.getLocation(), video.getLocation());
    }

    @Test
    public void testMapVideotoSuggestedVideoPreview() {
        Video v = video();
        SuggestedVideoPreview preview = mapper.mapVideotoSuggestedVideoPreview(v);

        assertEquals(v.getVideoid().toString(), preview.getVideoId().getValue());
        assertEquals(v.getUserid().toString(), preview.getUserId().getValue());
        assertEquals(v.getAddedDate(), timestampToInstant(preview.getAddedDate()));
        assertEquals(v.getName(), preview.getName());
        assertEquals(v.getPreviewImageLocation(), preview.getPreviewImageLocation());
    }

    @Test
    public void testParseGetRelatedVideosRequestData() {
        UUID videoid = UUID.randomUUID();
        GetRelatedVideosRequest request = GetRelatedVideosRequest.newBuilder()
                .setVideoId(uuidToUuid(videoid))
                .setPageSize(2)
                .setPagingState("Paging state")
                .build();

        GetRelatedVideosRequestData pojo = mapper.parseGetRelatedVideosRequestData(request);

        assertEquals(videoid, pojo.getVideoid());
        assertEquals(2, pojo.getPageSize());
        assertEquals("Paging state", pojo.getPagingState().get());
    }

    @Test
    public void testMapToGetRelatedVideosResponse() {
        Video v = video();
        UUID videoid = UUID.randomUUID();
        ResultListPage<Video> resultPage = new ResultListPage<>(singletonList(v), Optional.of("next page"));

        GetRelatedVideosResponse response = mapper.mapToGetRelatedVideosResponse(resultPage, videoid);
        assertEquals(videoid.toString(), response.getVideoId().getValue());
        assertEquals("next page", response.getPagingState());
        assertEquals(1, response.getVideosCount());
    }

    private Video video() {
        UUID videoid = UUID.randomUUID();
        UUID userid = UUID.randomUUID();
        Instant addedDate = Instant.now();
        String name = "Game";
        List<String> tagList = asList("tag1", "tag2");
        String previewLocation = "url";
        String location = "locationUrl";
        Video video = new Video();
        video.setVideoid(videoid);
        video.setUserid(userid);
        video.setName(name);
        video.setTags(new HashSet<>(tagList));
        video.setPreviewImageLocation(previewLocation);
        video.setLocation(location);
        video.setAddedDate(addedDate);
        return video;
    }
}