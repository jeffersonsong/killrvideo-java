package com.killrvideo.service.video.request

import java.time.Instant
import java.util.*

data class GetUserVideoPreviewsRequestData(
    val userId: UUID,
    val startingVideoId: UUID?,
    val startingAddedDate: Instant?,
    val pagingSize: Int?,
    val pagingState: String?
) {
    constructor(userId: UUID) : this(
        userId=userId,
        startingVideoId = null,
        startingAddedDate = null,
        pagingSize = null,
        pagingState = null
    )
}
