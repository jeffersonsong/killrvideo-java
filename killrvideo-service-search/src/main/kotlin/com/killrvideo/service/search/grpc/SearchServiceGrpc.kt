package com.killrvideo.service.search.grpc

import com.killrvideo.service.search.grpc.SearchServiceGrpcMapper.GetQuerySuggestionsRequestExtensions.parse
import com.killrvideo.service.search.grpc.SearchServiceGrpcMapper.SearchVideosRequestExtensions.parse
import com.killrvideo.service.search.repository.SearchRepository
import com.killrvideo.service.utils.ServiceGrpcUtils.trace
import killrvideo.search.SearchServiceGrpcKt
import killrvideo.search.SearchServiceOuterClass.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

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
    private val logger = KotlinLogging.logger {}

    /**
     * {@inheritDoc}
     */
    override suspend fun searchVideos(request: SearchVideosRequest):SearchVideosResponse {
        // Validate Parameters
        validator.validateGrpcRequest_SearchVideos(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val requestData = request.parse()

        // Map Result back to GRPC
        return kotlin.runCatching { searchRepository.searchVideosAsync(requestData) }
            .map { mapper.buildSearchGrpcResponse(it, request.query)}
            .trace(logger, "searchVideos", starts)
            .getOrThrow()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getQuerySuggestions(request: GetQuerySuggestionsRequest): GetQuerySuggestionsResponse {
        // Validate Parameters
        validator.validateGrpcRequest_GetQuerySuggestions(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val requestData = request.parse()

        // Invoke Dao (Async)
        return kotlin.runCatching { searchRepository.getQuerySuggestionsAsync(requestData) }
            .map { mapper.buildQuerySuggestionsResponse(it, requestData.query) }
            .trace(logger, "getQuerySuggestions", starts)
            .getOrThrow()
    }
}
