package com.killrvideo.service.statistic.grpc

import com.killrvideo.service.statistic.grpc.StatisticsServiceGrpcMapper.GetNumberOfPlaysRequestExtensions.parse
import com.killrvideo.service.statistic.repository.StatisticsRepository
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import killrvideo.statistics.StatisticsServiceGrpcKt
import killrvideo.statistics.StatisticsServiceOuterClass.*
import killrvideo.statistics.getNumberOfPlaysResponse
import killrvideo.statistics.recordPlaybackStartedResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Get statistics on a video.
 *
 * @author DataStax advocates Team
 */
@Service
class StatisticsServiceGrpc(
    private val statisticsRepository: StatisticsRepository,
    private val validator: StatisticsServiceGrpcValidator,
    private val mapper: StatisticsServiceGrpcMapper,
    @Value("\${killrvideo.discovery.services.statistic : StatisticsService}")
    val serviceKey: String
) : StatisticsServiceGrpcKt.StatisticsServiceCoroutineImplBase() {

    /** {@inheritDoc}  */
    override suspend fun recordPlaybackStarted(request: RecordPlaybackStartedRequest): RecordPlaybackStartedResponse {
        // Validate Parameters
        validator.validateGrpcRequest_RecordPlayback(request)

        // Mapping GRPC => Domain (Dao)
        val videoId = fromUuid(request.videoId)

        // Invoke DAO Async
        return runCatching { statisticsRepository.recordPlaybackStartedAsync(videoId) }
            .map { recordPlaybackStartedResponse {}}
            .getOrThrow()
    }

    /** {@inheritDoc}  */
    override suspend fun getNumberOfPlays(request: GetNumberOfPlaysRequest): GetNumberOfPlaysResponse {
        // Validate Parameters
        validator.validateGrpcRequest_GetNumberPlays(request)

        // Mapping GRPC => Domain (Dao)
        val listOfVideoId = request.parse()

        return if (listOfVideoId.isEmpty()) {
            getNumberOfPlaysResponse {}

        } else {
            // Invoke DAO Async
            runCatching { statisticsRepository.getNumberOfPlaysAsync(listOfVideoId) }
                .map { mapper.buildGetNumberOfPlayResponse(request, it) }
                .getOrThrow()
        }
    }
}
