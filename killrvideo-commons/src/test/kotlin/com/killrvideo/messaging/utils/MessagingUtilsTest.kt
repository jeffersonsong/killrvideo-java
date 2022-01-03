package com.killrvideo.messaging.utils;

import killrvideo.common.CommonEvents;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.killrvideo.messaging.utils.MessagingUtils.mapCustomError;
import static com.killrvideo.messaging.utils.MessagingUtils.mapError;
import static org.junit.jupiter.api.Assertions.*;

class MessagingUtilsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingUtils.class);

    @Test
    public void testMapError() {
        Throwable t = new RuntimeException("Something bad happened.");
        CommonEvents.ErrorEvent event = mapError(t);
        assertNotNull(event);
        LOGGER.info(event.toString());
    }

    @Test
    public void testMapCustomError() {
        CommonEvents.ErrorEvent event = mapCustomError("Custom error");
        assertNotNull(event);
        LOGGER.info(event.toString());
    }
}