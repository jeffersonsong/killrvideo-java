package com.killrvideo.utils;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.google.protobuf.Timestamp;

import killrvideo.common.CommonTypes.TimeUuid;
import killrvideo.common.CommonTypes.Uuid;

/**
 * Mapping.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class GrpcMappingUtils {
    
    /** Hiding private constructor. */
    private GrpcMappingUtils() {}
    
    /**
     * Conversions.
     */
    public static TimeUuid uuidToTimeUuid(UUID uuid) {
        return TimeUuid.newBuilder().setValue(uuid.toString()).build();
    }

    public static UUID fromTimeUuid(TimeUuid timeUuid) {
        return UUID.fromString(timeUuid.getValue());
    }
    
    public static Uuid uuidToUuid(UUID uuid) {
        return Uuid.newBuilder().setValue(uuid.toString()).build();
    }

    public static UUID fromUuid(Uuid uuid) {
        return UUID.fromString(uuid.getValue());
    }

    public static Uuid randomUuid() {
        return uuidToUuid(UUID.randomUUID());
    }

    public static TimeUuid randomTimeUuid() {
        return uuidToTimeUuid(Uuids.timeBased());
    }
    
    public static Date timestampToDate(Timestamp protoTimestamp) {
        return Date.from(timestampToInstant(protoTimestamp));
    }

    public static Timestamp dateToTimestamp(Date date) {
        return instantToTimeStamp(date.toInstant());
    }

    public static Instant timestampToInstant(Timestamp protoTimeStamp) {
        return Instant.ofEpochSecond(
                protoTimeStamp.getSeconds(),
                protoTimeStamp.getNanos() ) ;
    }

    public static Timestamp instantToTimeStamp(Instant instant) {
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }
}
