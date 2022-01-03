package com.killrvideo.service.search.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.search.dto.Video
import com.killrvideo.service.search.request.GetQuerySuggestionsRequestData
import com.killrvideo.service.search.request.SearchVideosRequestData
import com.killrvideo.utils.GrpcMappingUtils.instantToTimeStamp
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.search.SearchServiceOuterClass.*
import killrvideo.search.getQuerySuggestionsResponse
import killrvideo.search.searchResultsVideoPreview
import killrvideo.search.searchVideosResponse
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.springframework.stereotype.Component

/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
class SearchServiceGrpcMapper {

    object SearchVideosRequestExtensions {
        fun SearchVideosRequest.parse(): SearchVideosRequestData =
            SearchVideosRequestData(
                query = this.query,
                pageSize = this.pageSize,
                pagingState = if (isNotBlank(this.pagingState)) this.pagingState else null
            )
    }

    object GetQuerySuggestionsRequestExtensions {
        fun GetQuerySuggestionsRequest.parse(): GetQuerySuggestionsRequestData =
            GetQuerySuggestionsRequestData(
                query = this.query,
                pageSize = this.pageSize
            )
    }

    fun buildSearchGrpcResponse(
        resultPage: ResultListPage<Video>,
        _query: String
    ): SearchVideosResponse =
        searchVideosResponse {
            query = _query
            resultPage.pagingState?.let { pagingState = it }
            resultPage.results.stream()
                .filter { it != null }
                .map { maptoResultVideoPreview(it!!) }
                .forEach { videos.add(it) }
        }

    /**
     * Mapping to generated GPRC beans (Search result special).
     */
    private fun maptoResultVideoPreview(v: Video): SearchResultsVideoPreview =
        searchResultsVideoPreview {
            v.name?.let { name = it }
            v.videoid?.let { videoId = uuidToUuid(it) }
            v.userid?.let { userId = uuidToUuid(it) }
            v.previewImageLocation?.let { previewImageLocation = it }
            v.addedDate?.let { addedDate = instantToTimeStamp(it) }
        }

    fun buildQuerySuggestionsResponse(
        _suggestions: Iterable<String>, _query: String
    ): GetQuerySuggestionsResponse =
        getQuerySuggestionsResponse {
            suggestions.addAll(_suggestions)
            query = _query
        }
}
