package com.killrvideo.service.search.request

data class SearchVideosRequestData(
    val query: String,
    val pageSize: Int,
    val pagingState: String?
)
