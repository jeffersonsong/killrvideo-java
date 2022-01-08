package com.killrvideo.service.video.repository

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.dse.utils.MappedAsyncPagingIterableExtensions.all
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.dse.utils.PageableQueryFactory
import com.killrvideo.service.video.dao.UserVideoRowMapper
import com.killrvideo.service.video.dao.VideoCatalogMapper
import com.killrvideo.service.video.dto.LatestVideo
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository("VideoCatalogRepositoryImpl")
class VideoCatalogRepositoryImpl(
    pageableQueryFactory: PageableQueryFactory,
    mapper: VideoCatalogMapper,
    userVideoRowMapper: UserVideoRowMapper,
    private val latestVideoPreviewsRequestRepository: LatestVideoPreviewsRepository
): VideoCatalogRepository {
    private val videoDao = mapper.videoDao
    private val userVideoDao = mapper.userVideoDao
    private val latestVideoDao = mapper.latestVideoDao

    /**
     * Prepare Statements 'getUserVideo'.
     */
    private val findUserVideoPreviewStartingPoint: PageableQuery<UserVideo>
    private val findUserVideoPreviewNoStartingPoint: PageableQuery<UserVideo>

    init {
        findUserVideoPreviewStartingPoint = pageableQueryFactory.newPageableQuery(
            QUERY_USER_VIDEO_PREVIEW_STARTING_POINT,
            ConsistencyLevel.LOCAL_QUORUM
        ) { row: Row -> userVideoRowMapper.map(row) }

        findUserVideoPreviewNoStartingPoint = pageableQueryFactory.newPageableQuery(
            QUERY_USER_VIDEO_PREVIEW_NO_STARTING_POINT,
            ConsistencyLevel.LOCAL_QUORUM
        ) { row: Row -> userVideoRowMapper.map(row) }
    }

    /**
     * Insert a VIDEO in the DB (ASYNC).
     */
    override suspend fun insertVideoAsync(v: Video) {
        val now = Instant.now()
        v.addedDate = now
        CompletableFuture.allOf(
            videoDao.insert(v),
            userVideoDao.insert(UserVideo.from(v, now)),
            latestVideoDao.insert(LatestVideo.from(v, now))
        ).await()
    }

    override suspend fun getVideoById(videoid: UUID): Video? =
        videoDao.getVideoById(videoid).await()

    override suspend fun getVideoPreview(listofVideoId: List<UUID>): List<Video> =
        videoDao.getVideoPreview(listofVideoId)
            .thenApply {it.all()}.await()

    /**
     * Read a page of video preview for a user.
     *
     * @param request request.
     * @return requested video (page)
     */
    override suspend fun getUserVideosPreview(request: GetUserVideoPreviewsRequestData): ResultListPage<UserVideo> =
        if (request.startingVideoId != null && request.startingAddedDate != null) {
            findUserVideoPreviewStartingPoint.queryNext(
                request.pagingSize,
                request.pagingState,
                request.userId,
                request.startingAddedDate,
                request.startingVideoId
            ).await()
        } else {
            findUserVideoPreviewNoStartingPoint.queryNext(
                request.pagingSize,
                request.pagingState,
                request.userId
            ).await()
        }

    override suspend fun getLatestVideoPreviewsAsync(request: GetLatestVideoPreviewsRequestData): LatestVideosPage =
        latestVideoPreviewsRequestRepository.getLatestVideoPreviewsAsync(request)

    companion object {
        private val QUERY_USER_VIDEO_PREVIEW_STARTING_POINT =
            """
            SELECT * FROM killrvideo.user_videos 
            WHERE userid = :uid AND (added_date, videoid) <= (:ad, :vid) 
            """.trimIndent()
        private val QUERY_USER_VIDEO_PREVIEW_NO_STARTING_POINT =
            """
            SELECT * FROM killrvideo.user_videos 
            WHERE userid = :uid
            """.trimIndent()
    }
}
