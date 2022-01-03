package com.killrvideo.service.search.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.search.dto.Video
import com.killrvideo.service.search.repository.SearchRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
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
import org.springframework.beans.factory.annotation.Value

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
        val grpcReq = searchVideosRequest { }
        every { validator.validateGrpcRequest_SearchVideos(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.searchVideos(grpcReq) }
        }
    }

    @Test
    fun testSearchVideosWithQueryFailed() {
        val grpcReq = searchVideosRequest { }
        every { validator.validateGrpcRequest_SearchVideos(any()) } just Runs
        coEvery { searchRepository.searchVideosAsync(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service.searchVideos(grpcReq) }
        }
    }

    @Test
    fun testSearchVideos() {
        val grpcReq = searchVideosRequest { }
        every { validator.validateGrpcRequest_SearchVideos(any()) } just Runs
        val resultPage: ResultListPage<Video> = mockk()
        val response = searchVideosResponse {}
        every { mapper.buildSearchGrpcResponse(any(), any()) } returns response
        coEvery {
            searchRepository.searchVideosAsync(any())
        } returns resultPage
        val result = runBlocking { service.searchVideos(grpcReq) }
        assertEquals(response, result)
    }

    @Test
    fun testGetQuerySuggestionsWithValidationFailed() {
        val grpcReq = getQuerySuggestionsRequest {}
        every { validator.validateGrpcRequest_GetQuerySuggestions(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getQuerySuggestions(grpcReq) }
        }
    }

    @Test
    fun testGetQuerySuggestionsWithQueryFailed() {
        val grpcReq = getQuerySuggestionsRequest {}
        every { validator.validateGrpcRequest_GetQuerySuggestions(any()) } just Runs
        coEvery { searchRepository.getQuerySuggestionsAsync(any()) } throws Exception()
        assertThrows<Exception> {
            runBlocking { service!!.getQuerySuggestions(grpcReq) }
        }
    }

    @Test
    fun testGetQuerySuggestions() {
        val grpcReq = getQuerySuggestionsRequest {}
        every { validator.validateGrpcRequest_GetQuerySuggestions(any()) } just Runs
        val suggestionSet = setOf("Test")
        val response = getQuerySuggestionsResponse {}
        every {mapper.buildQuerySuggestionsResponse(any(),any())} returns response
        coEvery { searchRepository.getQuerySuggestionsAsync(any()) } returns suggestionSet
        val result = runBlocking { service.getQuerySuggestions(grpcReq) }
        assertEquals(response, result)
    }
}
