package com.killrvideo.service.video.repository;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;
import com.killrvideo.service.video.dao.UserVideoDao;
import com.killrvideo.service.video.dao.UserVideoRowMapper;
import com.killrvideo.service.video.dao.VideoCatalogMapper;
import com.killrvideo.service.video.dao.VideoDao;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.dto.UserVideo;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.killrvideo.utils.test.CassandraTestUtils.mockMappedAsyncPagingIterable;
import static com.killrvideo.utils.test.CassandraTestUtils.mockPageableQueryFactory;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoCatalogRepositoryTest {
    private VideoCatalogRepository repository;
    private VideoDao videoDao;
    private UserVideoDao userVideoDao;
    private LatestVideoPreviewsRepository latestVideoPreviewsRequestRepository;
    protected PageableQuery<UserVideo> findUserVideoPreview_startingPoint;
    protected PageableQuery<UserVideo> findUserVideoPreview_noStartingPoint;

    @BeforeEach
    public void setUp() {
        //noinspection unchecked
        this.findUserVideoPreview_startingPoint = mock(PageableQuery.class);
        this.findUserVideoPreview_noStartingPoint = mock(PageableQuery.class);
        PageableQueryFactory pageableQueryFactory = mockPageableQueryFactory(
                this.findUserVideoPreview_startingPoint,
                this.findUserVideoPreview_noStartingPoint
        );

        VideoCatalogMapper mapper = mock(VideoCatalogMapper.class);
        this.videoDao = mock(VideoDao.class);
        this.userVideoDao = mock(UserVideoDao.class);
        when(mapper.getVideoDao()).thenReturn(this.videoDao);
        when(mapper.getUserVideoDao()).thenReturn(this.userVideoDao);

        UserVideoRowMapper userVideoRowMapper = mock(UserVideoRowMapper.class);
        this.latestVideoPreviewsRequestRepository = mock(LatestVideoPreviewsRepository.class);

        this.repository = new VideoCatalogRepository(
                pageableQueryFactory, mapper, userVideoRowMapper, latestVideoPreviewsRequestRepository
        );
    }

    @Test
    public void testInsertVideoAsync() {
        Video v = mock(Video.class);
        when(this.videoDao.insert(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(this.userVideoDao.insert(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(this.latestVideoPreviewsRequestRepository.insert(any())).thenReturn(CompletableFuture.completedFuture(null));
        this.repository.insertVideoAsync(v).whenComplete((result, error) -> assertNull(error));
        verify(v, times(1)).setAddedDate(any());
        verify(this.videoDao, times(1)).insert(any());
        verify(this.userVideoDao, times(1)).insert(any());
        verify(this.latestVideoPreviewsRequestRepository, times(1)).insert(any());
    }

    @Test
    public void testGetVideoById() {
        UUID videoid = UUID.randomUUID();
        Video video = mock(Video.class);
        when(this.videoDao.getVideoById(any())).thenReturn(CompletableFuture.completedFuture(video));

        this.repository.getVideoById(videoid).whenComplete((result, error) -> assertEquals(video, result));
    }

    @Test
    public void testGetVideoPreview() {
        UUID videoid = UUID.randomUUID();
        Video video = mock(Video.class);
        List<UUID> videoids = singletonList(videoid);
        List<Video> videos = singletonList(video);
        MappedAsyncPagingIterable<Video> iter = mockMappedAsyncPagingIterable(videos);

        when(this.videoDao.getVideoPreview(any())).thenReturn(
                CompletableFuture.completedFuture(iter)
        );

        this.repository.getVideoPreview(videoids).whenComplete((result, error) -> assertEquals(videos, result));
    }

    @Test
    public void testGetUserVideosPreviewWithoutStartingPoint() {
        ResultListPage<UserVideo> resultListPage = mock(ResultListPage.class);
        when(this.findUserVideoPreview_noStartingPoint.queryNext(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(resultListPage));

        UUID userid = UUID.randomUUID();
        GetUserVideoPreviewsRequestData request = new GetUserVideoPreviewsRequestData(userid);
        this.repository.getUserVideosPreview(request).whenComplete((result, error) -> assertEquals(resultListPage, result));
    }

    @Test
    public void testGetUserVideosPreviewWithStartingPoint() {
        ResultListPage<UserVideo> resultListPage = mock(ResultListPage.class);
        when(this.findUserVideoPreview_startingPoint.queryNext(any(), any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(resultListPage));

        UUID userid = UUID.randomUUID();
        UUID videoid = UUID.randomUUID();
        Instant startingDate = Instant.now().minus(1, ChronoUnit.DAYS);

        GetUserVideoPreviewsRequestData request = new GetUserVideoPreviewsRequestData(
                userid,
                Optional.of(videoid),
                Optional.of(startingDate),
                Optional.of(2),
                Optional.empty()
        );
        this.repository.getUserVideosPreview(request).whenComplete((result, error) -> assertEquals(resultListPage, result));
    }

    @Test
    public void testGetLatestVideoPreviewsAsync() {
        GetLatestVideoPreviewsRequestData request = mock(GetLatestVideoPreviewsRequestData.class);
        LatestVideosPage latestVideosPage = mock(LatestVideosPage.class);
        when(latestVideoPreviewsRequestRepository.getLatestVideoPreviewsAsync(request))
                .thenReturn(CompletableFuture.completedFuture(latestVideosPage));

        this.repository.getLatestVideoPreviewsAsync(request).whenComplete((result, error) -> assertEquals(latestVideosPage, result));
    }
}
