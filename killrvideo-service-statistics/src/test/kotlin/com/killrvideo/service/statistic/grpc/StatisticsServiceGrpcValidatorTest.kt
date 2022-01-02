package com.killrvideo.service.statistic.grpc

import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import io.grpc.StatusRuntimeException
import killrvideo.statistics.getNumberOfPlaysRequest
import killrvideo.statistics.recordPlaybackStartedRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StatisticsServiceGrpcValidatorTest {
    private val validator = StatisticsServiceGrpcValidator()

    @Test
    fun testValidateGrpcRequest_GetNumberPlays_Success() {
        val request = getNumberOfPlaysRequest {
            videoIds.add(randomUuid())
        }
        validator.validateGrpcRequest_GetNumberPlays(request)
    }

    @Test
    fun testValidateGrpcRequest_GetNumberPlays_Failure() {
        val request = getNumberOfPlaysRequest {}
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequest_GetNumberPlays(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_RecordPlayback_Success() {
        val request = recordPlaybackStartedRequest {
            videoId = randomUuid()
        }
        validator.validateGrpcRequest_RecordPlayback(request)
    }

    @Test
    fun testValidateGrpcRequest_RecordPlayback_Failure() {
        val request = recordPlaybackStartedRequest {}
        assertThrows<StatusRuntimeException> {
            validator.validateGrpcRequest_RecordPlayback(request)
        }
    }
}
