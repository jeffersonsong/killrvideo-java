package com.killrvideo.utils

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import killrvideo.common.CommonTypes
import killrvideo.common.CommonTypes.TimeUuid
import killrvideo.common.timeUuid
import java.time.Instant
import java.util.*

/**
 * Mapping.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
object GrpcMappingUtils {
    /**
     * Conversions.
     */
    fun uuidToTimeUuid(uuid: UUID): TimeUuid =
        timeUuid {
            value = uuid.toString()
        }

    fun fromTimeUuid(timeUuid: TimeUuid): UUID =
        UUID.fromString(timeUuid.value)

    fun uuidToUuid(uuid: UUID): CommonTypes.Uuid =
        killrvideo.common.uuid {
            value = uuid.toString()
        }

    fun fromUuid(uuid: CommonTypes.Uuid): UUID =
        UUID.fromString(uuid.value)

    fun randomUuid(): CommonTypes.Uuid =
        uuidToUuid(UUID.randomUUID())

    fun randomTimeUuid(): TimeUuid =
        uuidToTimeUuid(Uuids.timeBased())

    fun timestampToDate(protoTimestamp: Timestamp): Date =
        Date.from(timestampToInstant(protoTimestamp))

    @JvmStatic
    fun dateToTimestamp(date: Date): Timestamp =
        instantToTimeStamp(date.toInstant())

    @JvmStatic
    fun timestampToInstant(protoTimeStamp: Timestamp): Instant =
        Instant.ofEpochSecond(
            protoTimeStamp.seconds,
            protoTimeStamp.nanos.toLong()
        )

    @JvmStatic
    fun instantToTimeStamp(instant: Instant): Timestamp =
        timestamp {
            seconds = instant.epochSecond
            nanos = instant.nano
        }
}
