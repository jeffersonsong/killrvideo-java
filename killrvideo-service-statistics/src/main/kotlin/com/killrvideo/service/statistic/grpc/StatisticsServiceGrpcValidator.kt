package com.killrvideo.service.statistic.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.common.CommonTypes
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class StatisticsServiceGrpcValidator {
    fun validateGrpcRequest_GetNumberPlays(request: GetNumberOfPlaysRequest) {
        val validator = FluentValidator.of("getNumberPlays", request, LOGGER)
            .notEmpty("video ids", request.videoIdsCount == 0)
            .error(
                "cannot do a get more than 20 videos at once for get number of plays request",
                request.videoIdsCount > 20
            )
        request.videoIdsList.forEach(
            Consumer { uuid: CommonTypes.Uuid? ->
                validator.error(
                    "provided UUID values cannot be null or blank for get number of plays request",
                    uuid == null || StringUtils.isBlank(uuid.value)
                )
            }
        )
        validator.validate()
    }

    fun validateGrpcRequest_RecordPlayback(request: RecordPlaybackStartedRequest) =
        FluentValidator.of("recordPlaybackStarted", request, LOGGER)
            .notEmpty("video id", StringUtils.isBlank(request.videoId.value))
            .validate()

    companion object {
        private val LOGGER = LoggerFactory.getLogger(StatisticsServiceGrpcValidator::class.java)
    }
}
