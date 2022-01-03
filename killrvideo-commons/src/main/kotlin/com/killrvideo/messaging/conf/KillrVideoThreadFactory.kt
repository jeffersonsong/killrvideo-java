package com.killrvideo.messaging.conf

import org.slf4j.LoggerFactory
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Custom ThreadFactory.
 *
 * @author DataStax Developer Advocates team.
 */
class KillrVideoThreadFactory
/**
 * Default constructor required for reflection.
 */
    : ThreadFactory {
    /** Counter keeping track of thread number in executor.  */
    private val threadNumber = AtomicInteger(10)

    /** {@inheritDoc}  */
    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r)
        thread.name = "killrvideo-default-executor-" + threadNumber.incrementAndGet()
        thread.isDaemon = true
        thread.uncaughtExceptionHandler = uncaughtExceptionHandler
        return thread
    }

    /**
     * Overriding error handling providing logging.
     */
    private val uncaughtExceptionHandler =
        UncaughtExceptionHandler { _: Thread, e: Throwable ->
            LOGGER.error("Uncaught asynchronous exception : " + e.message, e)
        }

    companion object {
        /** Create dedicated logger to trace ERRORS.  */
        private val LOGGER = LoggerFactory.getLogger("killrvideo-default-executor")
    }
}