package com.killrvideo.service.video.repository

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Collectors

@Repository("CachedVideoCatalogRepository")
class CachedVideoCatalogRepository(
    @Qualifier("VideoCatalogRepositoryImpl") private val videoCatalogRepository: VideoCatalogRepository,
    private val videoRedisRepository: VideoRedisRepository
): VideoCatalogRepository {
    override suspend fun insertVideoAsync(v: Video) {
        videoCatalogRepository.insertVideoAsync(v)
        videoRedisRepository.insertVideoAsync(v)
    }

    override suspend fun getVideoById(videoid: UUID): Video? {
        val cached = videoRedisRepository.getVideoById(videoid)
        return if (cached != null) {
            cached
        } else {
            val v = videoCatalogRepository.getVideoById(videoid)
            if (v != null) {
                videoRedisRepository.insertVideoAsync(v)
            }
            v
        }
    }

    override suspend fun getVideoPreview(listofVideoId: List<UUID>): List<Video> {
        val videosFromCache = videoRedisRepository.getVideoPreview(listofVideoId)
        val videioidsFromCache = videosFromCache.filter {it.videoid != null} .map { it.videoid }.toSet()
        val remainingVideoIds = listofVideoId.stream().filter {!videioidsFromCache.contains(it)}.collect(Collectors.toList())

        return if (remainingVideoIds.isEmpty()) {
            videosFromCache
        } else {
            val remainingVideos = videoCatalogRepository.getVideoPreview(remainingVideoIds)
            remainingVideos.forEach { videoRedisRepository.insertVideoAsync(it) }
            videosFromCache + remainingVideos
        }
    }

    override suspend fun getUserVideosPreview(request: GetUserVideoPreviewsRequestData): ResultListPage<UserVideo> {
        return videoCatalogRepository.getUserVideosPreview(request)
    }

    override suspend fun getLatestVideoPreviewsAsync(request: GetLatestVideoPreviewsRequestData): LatestVideosPage {
        return videoCatalogRepository.getLatestVideoPreviewsAsync(request)
    }
}
