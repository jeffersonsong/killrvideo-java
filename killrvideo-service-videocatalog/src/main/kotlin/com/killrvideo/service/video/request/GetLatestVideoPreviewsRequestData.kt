package com.killrvideo.service.video.request

import com.killrvideo.dse.dto.CustomPagingState
import java.time.Instant
import java.util.*

data class GetLatestVideoPreviewsRequestData(
    val pageState: CustomPagingState,
    val pageSize: Int,
    val startDate: Instant?,
    val startVideoId: UUID?
)
