package com.killrvideo.service.video.repository;

import com.killrvideo.dse.dto.CustomPagingState;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;
import com.killrvideo.service.video.dao.LatestVideoDao;
import com.killrvideo.service.video.dao.LatestVideoRowMapper;
import com.killrvideo.service.video.dao.VideoCatalogMapper;
import com.killrvideo.service.video.dto.LatestVideo;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.killrvideo.utils.test.CassandraTestUtils.mockPageableQueryFactory;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LatestVideoPreviewsRepositoryTest {
    private LatestVideoPreviewsRepository repository;
    private LatestVideoDao latestVideoDao;
    private PageableQuery<LatestVideo> findLatestVideoPreview_startingPoint;
    private PageableQuery<LatestVideo> findLatestVideoPreview_noStartingPoint;

    @BeforeEach
    public void setUp() {
        findLatestVideoPreview_startingPoint = mock(PageableQuery.class);
        findLatestVideoPreview_noStartingPoint = mock(PageableQuery.class);
        PageableQueryFactory pageableQueryFactory = mockPageableQueryFactory(
                findLatestVideoPreview_startingPoint,
                findLatestVideoPreview_noStartingPoint
        );

        this.latestVideoDao = mock(LatestVideoDao.class);
        VideoCatalogMapper mapper = mock(VideoCatalogMapper.class);
        when(mapper.getLatestVideoDao()).thenReturn(this.latestVideoDao);

        LatestVideoRowMapper latestVideoRowMapper = mock(LatestVideoRowMapper.class);

        this.repository = new LatestVideoPreviewsRepository(pageableQueryFactory, mapper, latestVideoRowMapper);
    }

    @Test
    public void testInsert() {
        when(this.latestVideoDao.insert(any())).thenReturn(
            CompletableFuture.completedFuture(null)
        );
        LatestVideo latestVideo = mock(LatestVideo.class);
        this.repository.insert(latestVideo).whenComplete((result, error) -> {
            assertNull(error);
        });
        verify(this.latestVideoDao, times(1)).insert(any());
    }

    @Test
    public void testGetLatestVideoPreviewsAsyncWithoutStartingPoint() {
        CustomPagingState cpState = CustomPagingState.buildFirstCustomPagingState();
        int pageSize = 2;

        GetLatestVideoPreviewsRequestData request = new GetLatestVideoPreviewsRequestData(
                cpState, pageSize, Optional.empty(), Optional.empty()
        );

        LatestVideo latestVideo1 = mock(LatestVideo.class);
        ResultListPage<LatestVideo> resultListPage1 = resultListPage(
                latestVideo1, "paging state 1"
        );
        LatestVideo latestVideo2 = mock(LatestVideo.class);
        ResultListPage<LatestVideo> resultListPage2 = resultListPage(
                latestVideo2, "paging state 2"
        );
        when(this.findLatestVideoPreview_noStartingPoint.queryNext(any(), any(), any()))
                .thenReturn(
                        CompletableFuture.completedFuture(resultListPage1),
                        CompletableFuture.completedFuture(resultListPage2)
                );

        repository.getLatestVideoPreviewsAsync(request).whenComplete((result, error) -> {
            assertNull(error);
            assertThat(result.getListOfPreview(), hasItems(latestVideo1, latestVideo2));
        });
    }

    @Test
    public void testGetLatestVideoPreviewsAsyncWithStartingPoint() {
        CustomPagingState cpState = CustomPagingState.buildFirstCustomPagingState();
        int pageSize = 2;
        UUID startingVideoid = UUID.randomUUID();
        Instant startingDate = Instant.now().minus(5, ChronoUnit.DAYS);

        GetLatestVideoPreviewsRequestData request = new GetLatestVideoPreviewsRequestData(
                cpState, pageSize, Optional.of(startingDate), Optional.of(startingVideoid)
        );

        LatestVideo latestVideo1 = mock(LatestVideo.class);
        ResultListPage<LatestVideo> resultListPage1 = resultListPage(
                latestVideo1, "paging state 1"
        );
        LatestVideo latestVideo2 = mock(LatestVideo.class);
        ResultListPage<LatestVideo> resultListPage2 = resultListPage(
                latestVideo2, "paging state 2"
        );
        when(this.findLatestVideoPreview_noStartingPoint.queryNext(any(), any(), any(), any(), any()))
                .thenReturn(
                        CompletableFuture.completedFuture(resultListPage1),
                        CompletableFuture.completedFuture(resultListPage2)
                );

        repository.getLatestVideoPreviewsAsync(request).whenComplete((result, error) -> {
            assertNull(error);
            assertThat(result.getListOfPreview(), hasItems(latestVideo1, latestVideo2));
        });
    }

    private ResultListPage<LatestVideo> resultListPage(LatestVideo latestVideo, String pagingState) {
        return new ResultListPage<LatestVideo>(
                singletonList(latestVideo), Optional.of(pagingState)
        );
    }
}