package com.killrvideo.service.statistic.grpc

import com.killrvideo.service.statistic.grpc.StatisticsServiceGrpcMapper.GetNumberOfPlaysRequestExtensions.parse
import com.killrvideo.service.statistic.repository.StatisticsRepository
import com.killrvideo.service.utils.ServiceGrpcUtils.trace
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import killrvideo.statistics.StatisticsServiceGrpcKt
import killrvideo.statistics.StatisticsServiceOuterClass.*
import killrvideo.statistics.getNumberOfPlaysResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Get statistics on a video.
 *
 * @author DataStax advocates Team
 */
@Service
class StatisticsServiceGrpc(
    private val statisticsRepository: StatisticsRepository,
    private val validator: StatisticsServiceGrpcValidator,
    private val mapper: StatisticsServiceGrpcMapper
) : StatisticsServiceGrpcKt.StatisticsServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return
     * current value of 'serviceKey'
     */
    @Value("\${killrvideo.discovery.services.statistic : StatisticsService}")
    val serviceKey: String? = null

    /** {@inheritDoc}  */
    override suspend fun recordPlaybackStarted(request: RecordPlaybackStartedRequest): RecordPlaybackStartedResponse {
        // Validate Parameters
        validator.validateGrpcRequest_RecordPlayback(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val videoId = fromUuid(request.videoId)

        // Invoke DAO Async
        return runCatching { statisticsRepository.recordPlaybackStartedAsync(videoId) }
            .map {
                RecordPlaybackStartedResponse.newBuilder().build()
            }.trace(logger, "recordPlaybackStarted", starts)
            .getOrThrow()
    }

    /** {@inheritDoc}  */
    override suspend fun getNumberOfPlays(request: GetNumberOfPlaysRequest): GetNumberOfPlaysResponse {
        // Validate Parameters
        validator.validateGrpcRequest_GetNumberPlays(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val listOfVideoId = request.parse()

        return if (listOfVideoId.isEmpty()) {
            getNumberOfPlaysResponse {}

        } else {
            // Invoke DAO Async
            runCatching { statisticsRepository.getNumberOfPlaysAsync(listOfVideoId) }
                .map { videoList ->
                    mapper.buildGetNumberOfPlayResponse(request, videoList)
                }.trace(logger, "getNumberOfPlays", starts)
                .getOrThrow()
        }
    }
}
