package com.killrvideo.service.search.grpc

import com.killrvideo.service.search.grpc.SearchServiceGrpcMapper.GetQuerySuggestionsRequestExtensions.parse
import com.killrvideo.service.search.grpc.SearchServiceGrpcMapper.SearchVideosRequestExtensions.parse
import com.killrvideo.service.search.repository.SearchRepository
import killrvideo.search.SearchServiceGrpcKt
import killrvideo.search.SearchServiceOuterClass.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Service SEARCG.
 *
 * @author DataStax advocates Team
 */
@Service
class SearchServiceGrpc(
    private val searchRepository: SearchRepository,
    private val validator: SearchServiceGrpcValidator,
    private val mapper: SearchServiceGrpcMapper,
    @Value("\${killrvideo.discovery.services.search : SearchService}")
    val serviceKey: String
) : SearchServiceGrpcKt.SearchServiceCoroutineImplBase() {

    /**
     * {@inheritDoc}
     */
    override suspend fun searchVideos(request: SearchVideosRequest):SearchVideosResponse {
        // Validate Parameters
        validator.validateGrpcRequest_SearchVideos(request)

        // Mapping GRPC => Domain (Dao)
        val requestData = request.parse()

        // Map Result back to GRPC
        return runCatching { searchRepository.searchVideosAsync(requestData) }
            .map { mapper.buildSearchGrpcResponse(it, request.query)}
            .getOrThrow()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getQuerySuggestions(request: GetQuerySuggestionsRequest): GetQuerySuggestionsResponse {
        // Validate Parameters
        validator.validateGrpcRequest_GetQuerySuggestions(request)

        // Mapping GRPC => Domain (Dao)
        val requestData = request.parse()

        // Invoke Dao (Async)
        return runCatching { searchRepository.getQuerySuggestionsAsync(requestData) }
            .map { mapper.buildQuerySuggestionsResponse(it, requestData.query) }
            .getOrThrow()
    }
}
