package com.killrvideo.service.search.request

data class GetQuerySuggestionsRequestData(
    val query: String,
    val pageSize: Int
)
