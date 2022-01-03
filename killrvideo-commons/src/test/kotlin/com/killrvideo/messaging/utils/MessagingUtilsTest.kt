package com.killrvideo.messaging.utils

import com.killrvideo.messaging.utils.MessagingUtils.mapCustomError
import com.killrvideo.messaging.utils.MessagingUtils.mapError
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class MessagingUtilsTest {
    private val logger = KotlinLogging.logger {  }
    @Test
    fun testMapError() {
        val t: Throwable = RuntimeException("Something bad happened.")
        val event = mapError(t)
        assertNotNull(event)
        logger.info(event.toString())
    }

    @Test
    fun testMapCustomError() {
        val event = mapCustomError("Custom error")
        assertNotNull(event)
        logger.info(event.toString())
    }
}
