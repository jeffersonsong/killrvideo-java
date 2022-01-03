package com.killrvideo.service.search.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.search.dto.Video
import com.killrvideo.service.search.grpc.SearchServiceGrpcMapper.GetQuerySuggestionsRequestExtensions.parse
import com.killrvideo.service.search.grpc.SearchServiceGrpcMapper.SearchVideosRequestExtensions.parse
import killrvideo.search.getQuerySuggestionsRequest
import killrvideo.search.searchVideosRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class SearchServiceGrpcMapperTest {
    private val mapper = SearchServiceGrpcMapper()
    @Test
    fun testBuildSearchGrpcResponse() {
        val v = Video(
            userid = UUID.randomUUID(),
            videoid = UUID.randomUUID(),
            name = "Game",
            previewImageLocation = "url",
            addedDate = Instant.now()
        )

        val resultPage = ResultListPage(listOf(v), "paging state")
        val response = mapper.buildSearchGrpcResponse(resultPage, "query")
        assertEquals("query", response.query)
        assertNotNull(resultPage.pagingState)
        assertEquals(resultPage.pagingState, response.pagingState)
        assertEquals(1, response.videosCount)
    }

    @Test
    fun testParseSearchVideosRequestData() {
        val request = searchVideosRequest {
            query = "Linux"
            pageSize = 5
            pagingState = "Paging state"
        }
        val pojo = request.parse()
        assertEquals(request.query, pojo.query)
        assertEquals(request.pageSize, pojo.pageSize)
        assertNotNull(pojo.pagingState)
        assertEquals(request.pagingState, pojo.pagingState)
    }

    @Test
    fun testBuildQuerySuggestionsResponse() {
        val suggestions = listOf("suggestion1", "suggestion2")
        val query = "query"
        val response = mapper.buildQuerySuggestionsResponse(suggestions, query)
        assertEquals(query, response.query)
        assertEquals(2, response.suggestionsCount)
    }

    @Test
    fun testParseGetQuerySuggestionsRequestData() {
        val request = getQuerySuggestionsRequest {
            query = "query"
            pageSize = 2
        }
        val pojo = request.parse()
        assertEquals("query", pojo.query)
        assertEquals(2, pojo.pageSize)
    }
}
