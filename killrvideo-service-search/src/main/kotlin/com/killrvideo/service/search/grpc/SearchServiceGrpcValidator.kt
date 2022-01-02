package com.killrvideo.service.search.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SearchServiceGrpcValidator {
    fun validateGrpcRequest_GetQuerySuggestions(request: GetQuerySuggestionsRequest) {
        FluentValidator.of("getQuerySuggestions", request, LOGGER)
            .notEmpty("query string", StringUtils.isBlank(request.query))
            .positive("page size", request.pageSize <= 0)
            .validate()
    }

    /**
     * Validation for search.
     */
    fun validateGrpcRequest_SearchVideos(request: SearchVideosRequest) {
        FluentValidator.of("searchVideos", request, LOGGER)
            .notEmpty("query string", StringUtils.isBlank(request.query))
            .positive("page size", request.pageSize <= 0)
            .validate()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SearchServiceGrpcValidator::class.java)
    }
}
