package com.killrvideo.utils

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.killrvideo.utils.GrpcMappingUtils.dateToTimestamp
import com.killrvideo.utils.GrpcMappingUtils.instantToTimeStamp
import com.killrvideo.utils.GrpcMappingUtils.timestampToDate
import com.killrvideo.utils.GrpcMappingUtils.timestampToInstant
import com.killrvideo.utils.GrpcMappingUtils.uuidToTimeUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.common.CommonTypes
import killrvideo.common.CommonTypes.TimeUuid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class GrpcMappingUtilsTest {
    @Test
    fun testUuidToTimeUuid() {
        val timebased = Uuids.timeBased()
        val result: TimeUuid = uuidToTimeUuid(timebased)
        assertEquals(timebased.toString(), result.value)
    }

    @Test
    fun testUuidToUuid() {
        val uuid = UUID.randomUUID()
        val result: CommonTypes.Uuid = uuidToUuid(uuid)
        assertEquals(uuid.toString(), result.value)
    }

    @Test
    fun testDateToTimestamp() {
        val now = Date()
        val ts = dateToTimestamp(now)
        val result: Date = timestampToDate(ts)
        assertEquals(now, result)
    }

    @Test
    fun testInstantToTimeStamp() {
        val now = Instant.now()
        val ts = instantToTimeStamp(now)
        val result = timestampToInstant(ts)
        assertEquals(now, result)
    }
}