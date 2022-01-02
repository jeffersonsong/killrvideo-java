package com.killrvideo.service.search.grpc

import io.grpc.StatusRuntimeException
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest
import killrvideo.search.getQuerySuggestionsRequest
import killrvideo.search.searchVideosRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SearchServiceGrpcValidatorTest {
    private val validator = SearchServiceGrpcValidator()

    @Test
    fun testValidateGrpcRequest_GetQuerySuggestions_Success() {
        val request = getQuerySuggestionsRequest {
            query = "Query"
            pageSize = 2
        }
        validator.validateGrpcRequest_GetQuerySuggestions(request)
    }

    @Test
    fun testValidateGrpcRequest_GetQuerySuggestions_Failure() {
        val request = GetQuerySuggestionsRequest.getDefaultInstance()
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequest_GetQuerySuggestions(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_SearchVideos_Success() {
        val request = searchVideosRequest {
            query = "Query"
            pageSize = 2
        }
        validator.validateGrpcRequest_SearchVideos(request)
    }

    @Test
    fun testValidateGrpcRequest_SearchVideos_Failure() {
        val request = SearchVideosRequest.getDefaultInstance()
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequest_SearchVideos(request)
        }
    }
}
