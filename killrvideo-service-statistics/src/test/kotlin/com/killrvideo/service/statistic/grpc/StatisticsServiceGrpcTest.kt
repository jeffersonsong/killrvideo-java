package com.killrvideo.service.statistic.grpc

import com.killrvideo.service.statistic.dto.VideoPlaybackStats
import com.killrvideo.service.statistic.repository.StatisticsRepository
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse
import killrvideo.statistics.getNumberOfPlaysRequest
import killrvideo.statistics.recordPlaybackStartedRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Value
import java.util.*

internal class StatisticsServiceGrpcTest {
    @InjectMockKs
    private lateinit var service: StatisticsServiceGrpc

    @MockK
    private lateinit var statisticsRepository: StatisticsRepository

    @MockK
    private lateinit var validator: StatisticsServiceGrpcValidator

    @MockK
    private lateinit var mapper: StatisticsServiceGrpcMapper

    val serviceKey = "StatisticsService"

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testRecordPlaybackStartedWithValidationFailure() {
        val grpcReq = recordPlaybackStartedRequest {}
        every { validator.validateGrpcRequest_RecordPlayback(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.recordPlaybackStarted(grpcReq) }
        }
    }

    @Test
    fun testRecordPlaybackStartedWithQueryFailure() {
        val grpcReq = recordPlaybackStartedRequest { videoId = randomUuid() }
        every { validator.validateGrpcRequest_RecordPlayback(any()) } just Runs
        coEvery { statisticsRepository.recordPlaybackStartedAsync(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.recordPlaybackStarted(grpcReq) }
        }
    }

    @Test
    fun testRecordPlayback() {
        val grpcReq = recordPlaybackStartedRequest { videoId = randomUuid() }
        every { validator.validateGrpcRequest_RecordPlayback(any()) } just Runs
        coEvery { statisticsRepository.recordPlaybackStartedAsync(any()) } returns 1
        runBlocking { service.recordPlaybackStarted(grpcReq) }
    }

    @Test
    fun testGetNumberOfPlaysWithValidationFailure() {
        val grpcReq = getNumberOfPlaysRequest {}
        every { validator.validateGrpcRequest_GetNumberPlays(any()) } throws
                Status.INVALID_ARGUMENT.asRuntimeException()
        assertThrows<StatusRuntimeException> {
            runBlocking { service.getNumberOfPlays(grpcReq) }
        }
    }

    @Test
    fun testGetNumberOfPlaysWithQueryFailure() {
        val videoid = UUID.randomUUID()
        val grpcReq = getNumberOfPlaysRequest {
            videoIds.add(uuidToUuid(videoid))
        }
        every { validator.validateGrpcRequest_GetNumberPlays(any()) } just Runs
        coEvery { statisticsRepository.getNumberOfPlaysAsync(any()) } throws Exception()

        assertThrows<Exception> {
            runBlocking { service.getNumberOfPlays(grpcReq) }
        }
    }

    @Test
    fun testGetNumberOfPlaysWithEmptyVideIdsList() {
        val grpcReq = getNumberOfPlaysRequest {}
        every { validator.validateGrpcRequest_GetNumberPlays(any()) } just Runs

        val result = runBlocking { service.getNumberOfPlays(grpcReq) }
        assertEquals(0, result.statsCount)
    }

    @Test
    fun testGetNumberOfPlays() {
        val videoid = UUID.randomUUID()
        val grpcReq = getNumberOfPlaysRequest {
            videoIds.add(uuidToUuid(videoid))
        }
        every { validator.validateGrpcRequest_GetNumberPlays(any()) } just Runs
        val videoList = emptyList<VideoPlaybackStats>()
        coEvery { statisticsRepository.getNumberOfPlaysAsync(any()) } returns videoList

        val response = GetNumberOfPlaysResponse.getDefaultInstance()
        every { mapper.buildGetNumberOfPlayResponse(any(), any()) } returns response

        val result = runBlocking { service.getNumberOfPlays(grpcReq) }
        assertEquals(response, result)
    }
}
