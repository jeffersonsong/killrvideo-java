package com.killrvideo.utils;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import killrvideo.common.CommonTypes.*;
import org.junit.jupiter.api.Test;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class GrpcMappingUtilsTest {

    @Test
    public void testUuidToTimeUuid() {
        UUID timebased = Uuids.timeBased();
        TimeUuid result = uuidToTimeUuid(timebased);
        assertEquals(timebased.toString(), result.getValue());
    }

    @Test
    public void testUuidToUuid() {
        UUID uuid = UUID.randomUUID();
        Uuid result = uuidToUuid(uuid);
        assertEquals(uuid.toString(), result.getValue());
    }

    @Test
    public void testDateToTimestamp() {
        Date now = new Date();
        Timestamp ts = dateToTimestamp(now);
        Date result = timestampToDate(ts);
        assertEquals(now, result);
    }

    @Test
    public void testInstantToTimeStamp() {
        Instant now = Instant.now();
        Timestamp ts = instantToTimeStamp(now);
        Instant result = timestampToInstant(ts);
        assertEquals(now, result);
    }
}