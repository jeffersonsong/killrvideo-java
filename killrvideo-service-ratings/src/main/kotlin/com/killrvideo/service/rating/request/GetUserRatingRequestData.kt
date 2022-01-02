package com.killrvideo.service.rating.request

import java.util.*

data class GetUserRatingRequestData(
    val videoid: UUID,
    val userid: UUID
)
