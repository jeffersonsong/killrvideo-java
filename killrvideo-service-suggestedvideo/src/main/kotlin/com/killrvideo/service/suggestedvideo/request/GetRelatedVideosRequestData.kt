package com.killrvideo.service.suggestedvideo.request

import java.util.*

data class GetRelatedVideosRequestData(
    val videoid: UUID,
    val pageSize:Int,
    val pagingState: String?
)
