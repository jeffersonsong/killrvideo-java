package com.killrvideo.dse.dto

import com.killrvideo.dse.dto.CustomPagingStateTest
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CustomPagingStateTest {
    private val logger = KotlinLogging.logger {  }
    @Test
    fun testCustomPagingState() {
        val firstState = CustomPagingState.buildFirstCustomPagingState()
        val state = firstState.changeCassandraPagingState("CassamdraState")
        val serialized = state.serialize()
        logger.info {"serialized: $serialized"}
        logger.info {"toString  : $state"}
        val deserialized: CustomPagingState? = CustomPagingState.deserialize(serialized)
        assertNotNull(deserialized)
        val parsed = deserialized!!
        assertEquals(state.currentBucket, parsed.currentBucket)
        assertEquals(state.listOfBucketsSize, parsed.listOfBucketsSize)
        assertEquals(state.listOfBuckets, parsed.listOfBuckets)
        assertEquals(state.cassandraPagingState, parsed.cassandraPagingState)
    }

    @Test
    fun testGetCurrentBucketValue() {
        val now = Instant.now()
        var state = CustomPagingState.buildFirstCustomPagingState()
        assertEquals(format(now), state.currentBucketValue)
        state = state.incCurrentBucketIndex()
        val yesterday = now.minus(1, ChronoUnit.DAYS)
        assertEquals(format(yesterday), state.currentBucketValue)
    }

    private fun format(instant: Instant): String {
        return DATEFORMATTER.format(instant)
    }

    companion object {
        private val DATEFORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC))
    }
}
