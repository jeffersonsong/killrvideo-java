package com.killrvideo.service.video.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.dse.utils.MappedAsyncPagingIterableUtils;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;
import com.killrvideo.service.video.dao.*;
import com.killrvideo.service.video.dto.LatestVideo;
import com.killrvideo.service.video.dto.LatestVideosPage;
import com.killrvideo.service.video.dto.UserVideo;
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData;
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
public class VideoCatalogRepository {
    private static final String QUERY_USER_VIDEO_PREVIEW_STARTING_POINT =
            "SELECT * " +
                    "FROM killrvideo.user_videos " +
                    "WHERE userid = :uid " +
                    "AND (added_date, videoid) <= (:ad, :vid)";
    private static final String QUERY_USER_VIDEO_PREVIEW_NO_STARTING_POINT =
            "SELECT * " +
                    "FROM killrvideo.user_videos " +
                    "WHERE userid = :uid ";


    private final VideoDao videoDao;
    private final UserVideoDao userVideoDao;
    private final LatestVideoPreviewsRepository latestVideoPreviewsRequestRepository;

    /**
     * Prepare Statements 'getUserVideo'.
     */
    protected final PageableQuery<UserVideo> findUserVideoPreview_startingPoint;
    protected final PageableQuery<UserVideo> findUserVideoPreview_noStartingPoint;

    public VideoCatalogRepository(PageableQueryFactory pageableQueryFactory,
                                  VideoCatalogMapper mapper,
                                  UserVideoRowMapper userVideoRowMapper,
                                  LatestVideoPreviewsRepository latestVideoPreviewsRequestHandler) {
        this.latestVideoPreviewsRequestRepository = latestVideoPreviewsRequestHandler;
        this.videoDao = mapper.getVideoDao();
        this.userVideoDao = mapper.getUserVideoDao();

        this.findUserVideoPreview_startingPoint = pageableQueryFactory.newPageableQuery(
                QUERY_USER_VIDEO_PREVIEW_STARTING_POINT,
                ConsistencyLevel.LOCAL_QUORUM,
                userVideoRowMapper::map
        );
        this.findUserVideoPreview_noStartingPoint = pageableQueryFactory.newPageableQuery(
                QUERY_USER_VIDEO_PREVIEW_NO_STARTING_POINT,
                ConsistencyLevel.LOCAL_QUORUM,
                userVideoRowMapper::map
        );
    }

    /**
     * Insert a VIDEO in the DB (ASYNC).
     */
    public CompletableFuture<Void> insertVideoAsync(Video v) {
        Instant now = Instant.now();
        v.setAddedDate(now);

        return CompletableFuture.allOf(
                this.videoDao.insert(v),
                this.userVideoDao.insert(UserVideo.from(v, now)),
                this.latestVideoPreviewsRequestRepository.insert(LatestVideo.from(v, now))
        );
    }

    public CompletableFuture<Video> getVideoById(UUID videoid) {
        return this.videoDao.getVideoById(videoid);
    }

    public CompletableFuture<List<Video>> getVideoPreview(List<UUID> listofVideoId) {
        Assert.notNull(listofVideoId, "videoid list cannot be null");

        return this.videoDao.getVideoPreview(listofVideoId)
                .thenApply(MappedAsyncPagingIterableUtils::all);
    }

    /**
     * Read a page of video preview for a user.
     *
     * @param request request.
     * @return requested video (page)
     */
    public CompletableFuture<ResultListPage<UserVideo>> getUserVideosPreview(GetUserVideoPreviewsRequestData request) {
        if (request.getStartingVideoId().isPresent() && request.getStartingAddedDate().isPresent()) {
            return getUserVideosPreviewWithStartingPoint(request);
        } else {
            return getUserVideosPreviewWithoutStartingPoint(request);
        }
    }

    private CompletableFuture<ResultListPage<UserVideo>> getUserVideosPreviewWithStartingPoint(
            GetUserVideoPreviewsRequestData request
    ) {
        return findUserVideoPreview_startingPoint.queryNext(
                request.getPagingSize(),
                request.getPagingState(),
                request.getUserId(),
                request.getStartingAddedDate().get(),
                request.getStartingVideoId().get()
        );
    }

    private CompletableFuture<ResultListPage<UserVideo>> getUserVideosPreviewWithoutStartingPoint(
            GetUserVideoPreviewsRequestData request
    ) {
        return findUserVideoPreview_noStartingPoint.queryNext(
                request.getPagingSize(),
                request.getPagingState(),
                request.getUserId()
        );
    }

    public CompletableFuture<LatestVideosPage> getLatestVideoPreviewsAsync(
            GetLatestVideoPreviewsRequestData request
    ) {
        return latestVideoPreviewsRequestRepository.getLatestVideoPreviewsAsync(request);
    }
}
