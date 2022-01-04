package com.killrvideo.service.search.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.search.dto.Video
import com.killrvideo.service.search.repository.SearchRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import killrvideo.search.getQuerySuggestionsRequest
import killrvideo.search.getQuerySuggestionsResponse
import killrvideo.search.searchVideosRequest
import killrvideo.search.searchVideosResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SearchServiceGrpcTest {
    @InjectMockKs
    private lateinit var service: SearchServiceGrpc

    @MockK
    private lateinit var searchRepository: SearchRepository

    @MockK
    private lateinit var validator: SearchServiceGrpcValidator

    @MockK
    private lateinit var mapper: SearchServiceGrpcMapper
    private val serviceKey = "SearchService"

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testSearchVideosWithValidationFailed() {
        val request = searchVideosRequest { }
        every { validator.validateGrpcRequest_SearchVideos(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.searchVideos(request) }
        }
    }

    @Test
    fun testSearchVideosWithQueryFailed() {
        val request = searchVideosRequest { }
        every { validator.validateGrpcRequest_SearchVideos(any()) } just Runs
        coEvery { searchRepository.searchVideosAsync(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.searchVideos(request) }
        }
    }

    @Test
    fun testSearchVideos() {
        val request = searchVideosRequest { }
        every { validator.validateGrpcRequest_SearchVideos(any()) } just Runs
        val resultPage: ResultListPage<Video> = mockk()
        val response = searchVideosResponse {}
        every { mapper.buildSearchGrpcResponse(any(), any()) } returns response
        coEvery {
            searchRepository.searchVideosAsync(any())
        } returns resultPage
        val result = runBlocking { service.searchVideos(request) }
        assertEquals(response, result)
    }

    @Test
    fun testGetQuerySuggestionsWithValidationFailed() {
        val request = getQuerySuggestionsRequest {}
        every { validator.validateGrpcRequest_GetQuerySuggestions(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getQuerySuggestions(request) }
        }
    }

    @Test
    fun testGetQuerySuggestionsWithQueryFailed() {
        val request = getQuerySuggestionsRequest {}
        every { validator.validateGrpcRequest_GetQuerySuggestions(any()) } just Runs
        coEvery { searchRepository.getQuerySuggestionsAsync(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.getQuerySuggestions(request) }
        }
    }

    @Test
    fun testGetQuerySuggestions() {
        val request = getQuerySuggestionsRequest {}
        every { validator.validateGrpcRequest_GetQuerySuggestions(any()) } just Runs
        val suggestionSet = setOf("Test")
        val response = getQuerySuggestionsResponse {}
        every {mapper.buildQuerySuggestionsResponse(any(),any())} returns response
        coEvery { searchRepository.getQuerySuggestionsAsync(any()) } returns suggestionSet
        val result = runBlocking { service.getQuerySuggestions(request) }
        assertEquals(response, result)
    }
}
