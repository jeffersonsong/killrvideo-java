package com.killrvideo.service.video.request

import java.time.Instant
import java.util.*

data class GetLatestVideoPreviewsForGivenDateRequestData(
    val yyyymmdd: String,
    val pagingState: String?,
    val pageSize: Int,
    val startDate: Instant?,
    val startVideoId: UUID?
)
