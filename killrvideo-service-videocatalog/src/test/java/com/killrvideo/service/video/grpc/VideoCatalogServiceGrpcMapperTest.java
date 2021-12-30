package com.killrvideo.service.video.grpc;

import com.killrvideo.dse.dto.CustomPagingState;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.service.video.dto.LatestVideo;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.dto.UserVideo;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import killrvideo.video_catalog.events.VideoCatalogEvents;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.instantToTimeStamp;
import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VideoCatalogServiceGrpcMapperTest {
    private final VideoCatalogServiceGrpcMapper mapper = new VideoCatalogServiceGrpcMapper();

    @Test
    public void testMapSubmitYouTubeVideoRequestAsVideo() {
        Video v = video();
        SubmitYouTubeVideoRequest request = SubmitYouTubeVideoRequest.newBuilder()
                .setVideoId(uuidToUuid(v.getVideoid()))
                .setUserId(uuidToUuid(v.getUserid()))
                .setName(v.getName())
                .setDescription(v.getDescription())
                .setYouTubeVideoId(v.getLocation())
                .addAllTags(v.getTags())
                .build();

        Video video = mapper.mapSubmitYouTubeVideoRequestAsVideo(request);
        assertEquals(v.getVideoid(), video.getVideoid());
        assertEquals(v.getUserid(), video.getUserid());
        assertEquals(v.getName(), video.getName());
        assertEquals(v.getLocation(), video.getLocation());
        assertEquals(v.getDescription(), video.getDescription());
        assertEquals(v.getTags().size(), video.getTags().size());
        assertEquals(v.getLocationType(), video.getLocationType());
    }

    @Test
    public void testMapLatestVideoToGrpcResponse() {
        Video v = video();
        LatestVideo latestVideo = LatestVideo.from(v, Instant.now());
        String nextPageState = "next page state";
        String cassandraPagingState = "cassandra paging state";
        LatestVideosPage latestVideosPage = new LatestVideosPage(singletonList(latestVideo),
                cassandraPagingState,
                nextPageState);

        GetLatestVideoPreviewsResponse response = mapper.mapLatestVideoToGrpcResponse(latestVideosPage);
        assertEquals(nextPageState, response.getPagingState());
        assertEquals(1, response.getVideoPreviewsCount());
    }

    @Test
    public void testMapFromVideotoVideoResponse() {
        Video v = video();
        GetVideoResponse response = mapper.mapFromVideotoVideoResponse(v);

        assertEquals(v.getVideoid().toString(), response.getVideoId().getValue());
        assertEquals(v.getDescription(), response.getDescription());
        assertEquals(v.getLocation(), response.getLocation());
        assertEquals(v.getLocationType(), response.getLocationType().getNumber());
        assertEquals(v.getName(), response.getName());
        assertEquals(v.getUserid().toString(), response.getUserId().getValue());
        assertEquals(v.getTags().size(), response.getTagsCount());
    }

    @Test
    public void testCreateYouTubeVideoAddedEvent() {
        Video v = video();
        VideoCatalogEvents.YouTubeVideoAdded event = mapper.createYouTubeVideoAddedEvent(v);

        assertEquals(v.getVideoid().toString(), event.getVideoId().getValue());
        assertEquals(v.getDescription(), event.getDescription());
        assertEquals(v.getLocation(), event.getLocation());
        assertEquals(v.getName(), event.getName());
        assertEquals(v.getUserid().toString(), event.getUserId().getValue());
    }

    @Test
    public void testParseGetLatestVideoPreviewsRequest() {
        UUID startingVidoId = UUID.randomUUID();
        GetLatestVideoPreviewsRequest request = GetLatestVideoPreviewsRequest.newBuilder()
                .setPageSize(2)
                .setStartingVideoId(uuidToUuid(startingVidoId))
                .build();

        GetLatestVideoPreviewsRequestData pojo = mapper.parseGetLatestVideoPreviewsRequest(
                request,
                CustomPagingState::buildFirstCustomPagingState
        );

        assertEquals(2, pojo.getPageSize());
        assertNotNull(pojo.getPageState());
        assertEquals(startingVidoId, pojo.getStartVideoId().get());
    }

    @Test
    public void testMapToGetVideoPreviewsResponse() {
        Video v = video();
        GetVideoPreviewsResponse response = mapper.mapToGetVideoPreviewsResponse(singletonList(v));

        assertEquals(1, response.getVideoPreviewsCount());
        VideoPreview preview = response.getVideoPreviews(0);

        assertEquals(v.getName(), preview.getName());
        assertEquals(v.getUserid().toString(), preview.getUserId().getValue());
        assertEquals(v.getVideoid().toString(), preview.getVideoId().getValue());
    }

    @Test
    public void testParseGetUserVideoPreviewsRequest() {
        UUID userid = UUID.randomUUID();
        UUID startingVideioId = UUID.randomUUID();
        Instant startingAddedDate = Instant.now().minus(1L, ChronoUnit.DAYS);
        int pageSize = 2;
        String pagingState = "Paging state";

        GetUserVideoPreviewsRequest request = GetUserVideoPreviewsRequest.newBuilder()
                .setUserId(uuidToUuid(userid))
                .setStartingVideoId(uuidToUuid(startingVideioId))
                .setStartingAddedDate(instantToTimeStamp(startingAddedDate))
                .setPageSize(pageSize)
                .setPagingState(pagingState)
                .build();

        GetUserVideoPreviewsRequestData pojo = mapper.parseGetUserVideoPreviewsRequest(request);

        assertEquals(userid, pojo.getUserId());
        assertEquals(startingVideioId, pojo.getStartingVideoId().get());
        assertEquals(startingAddedDate, pojo.getStartingAddedDate().get());
        assertEquals(pageSize, pojo.getPagingSize().get());
        assertEquals(pagingState, pojo.getPagingState().get());
    }

    @Test
    public void testMapToGetUserVideoPreviewsResponse() {
        UUID userid = UUID.randomUUID();
        Video v = video();
        UserVideo userVideo = UserVideo.from(v, Instant.now());
        String pagingState = "Paging state";
        ResultListPage<UserVideo> resultListPage = new ResultListPage<>(
                singletonList(userVideo),
                Optional.of(pagingState)
        );

        GetUserVideoPreviewsResponse response = mapper.mapToGetUserVideoPreviewsResponse(resultListPage, userid);
        assertEquals(pagingState, response.getPagingState());
        assertEquals(1, response.getVideoPreviewsCount());
    }

    private Video video() {
        Video v = new Video();
        v.setVideoid(UUID.randomUUID());
        v.setUserid(UUID.randomUUID());
        v.setName("Game");
        v.setLocation("url");
        v.setDescription("Description");
        v.setTags(new HashSet<>(asList("tag1", "tag2")));
        v.setPreviewImageLocation("previewUrl");
        v.setLocationType(VideoLocationType.YOUTUBE.ordinal());
        v.setAddedDate(Instant.now());
        return v;
    }
}
