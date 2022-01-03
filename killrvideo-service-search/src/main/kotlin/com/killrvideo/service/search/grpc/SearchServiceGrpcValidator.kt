package com.killrvideo.service.search.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@Component
class SearchServiceGrpcValidator {
    private val logger = KotlinLogging.logger {  }

    fun validateGrpcRequest_GetQuerySuggestions(request: GetQuerySuggestionsRequest) {
        FluentValidator.of("getQuerySuggestions", request, logger)
            .notEmpty("query string", StringUtils.isBlank(request.query))
            .positive("page size", request.pageSize <= 0)
            .validate()
    }

    /**
     * Validation for search.
     */
    fun validateGrpcRequest_SearchVideos(request: SearchVideosRequest) {
        FluentValidator.of("searchVideos", request, logger)
            .notEmpty("query string", StringUtils.isBlank(request.query))
            .positive("page size", request.pageSize <= 0)
            .validate()
    }
}
