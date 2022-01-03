package com.killrvideo.utils

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.google.protobuf.Timestamp
import killrvideo.common.CommonTypes
import killrvideo.common.CommonTypes.TimeUuid
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
        TimeUuid.newBuilder().setValue(uuid.toString()).build()

    fun fromTimeUuid(timeUuid: TimeUuid): UUID =
        UUID.fromString(timeUuid.value)

    fun uuidToUuid(uuid: UUID): CommonTypes.Uuid =
        CommonTypes.Uuid.newBuilder().setValue(uuid.toString()).build()

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
        Timestamp.newBuilder().setSeconds(instant.epochSecond).setNanos(instant.nano).build()
}
