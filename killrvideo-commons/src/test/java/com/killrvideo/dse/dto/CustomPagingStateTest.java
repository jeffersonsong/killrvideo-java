package com.killrvideo.dse.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CustomPagingStateTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPagingStateTest.class);
    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC));

    @BeforeEach
    void setUp() {
    }

    @Test
    public void testCustomPagingState() {
        CustomPagingState firstState = CustomPagingState.buildFirstCustomPagingState();
        CustomPagingState state = new CustomPagingState(firstState.getListOfBuckets(), 0, "CassamdraState");
        String serialized = CustomPagingState.createPagingState(state.getListOfBuckets(), state.getCurrentBucket(), state.getCassandraPagingState());
        LOGGER.info("serialized: " + serialized);
        LOGGER.info("toString  : " + state);
        CustomPagingState parsed = CustomPagingState.parse(serialized).get();

        assertEquals(state.getCurrentBucket(), parsed.getCurrentBucket());
        assertEquals(state.getListOfBucketsSize(), parsed.getListOfBucketsSize());
        assertEquals(state.getListOfBuckets(), parsed.getListOfBuckets());
        assertEquals(state.getCassandraPagingState(), parsed.getCassandraPagingState());
    }

    @Test
    public void testGetCurrentBucketValue() {
        Instant now = Instant.now();
        CustomPagingState state = CustomPagingState.buildFirstCustomPagingState();

        assertEquals(format(now), state.getCurrentBucketValue());
        state.incCurrentBucketIndex();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        assertEquals(format(yesterday), state.getCurrentBucketValue());
    }

    private String format(Instant instant) {
        return DATEFORMATTER.format(instant);
    }
}
