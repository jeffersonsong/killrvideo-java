package com.killrvideo.service.video.repository

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData
import java.util.*

interface VideoCatalogRepository {
    suspend fun insertVideoAsync(v: Video)

    suspend fun getVideoById(videoid: UUID): Video?

    suspend fun getVideoPreview(listofVideoId: List<UUID>): List<Video>

    suspend fun getUserVideosPreview(request: GetUserVideoPreviewsRequestData): ResultListPage<UserVideo>

    suspend fun getLatestVideoPreviewsAsync(request: GetLatestVideoPreviewsRequestData): LatestVideosPage
}