package com.killrvideo.messaging.dao

import com.killrvideo.messaging.utils.MessagingUtils.mapError
import io.mockk.*
import killrvideo.common.CommonEvents.ErrorEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.PrintWriter

internal class ErrorProcessorTest {
    private lateinit var processor: ErrorProcessor
    private lateinit var printWriter: PrintWriter

    @BeforeEach
    fun setUp() {
        printWriter = mockk()
        every { printWriter.append(any<String>()) } returns printWriter
        every { printWriter.flush() } just Runs
        every { printWriter.close() } just Runs
        processor = ErrorProcessor(printWriter)
    }

    @Test
    fun testHandle() {
        val t: Throwable = RuntimeException("Something bad happened.")
        val event: ErrorEvent = mapError(t)
        processor.handle(event)
        processor.closeErrorLogFile()
        verify(exactly = 2) {
            printWriter.append(any<String>())
        }
        verify(exactly = 1) {
            printWriter.flush()
            printWriter.close()
        }
    }
}