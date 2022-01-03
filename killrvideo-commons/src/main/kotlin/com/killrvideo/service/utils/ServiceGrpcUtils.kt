package com.killrvideo.service.utils

import mu.KLogger
import java.time.Duration
import java.time.Instant
import java.util.*

object ServiceGrpcUtils {

    fun <T> Result<T>.trace(logger: KLogger, method: String, starts: Instant): Result<T> {
        this.onSuccess {
            traceSuccess(logger, method, starts)
        }.onFailure { error ->
            traceError(logger, method, starts, error)
        }
        return this
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    fun traceSuccess(logger: KLogger, method: String, starts: Instant) {
        logger.debug {
            "End successfully '$method' in ${Duration.between(starts, Instant.now()).nano / 1000} millis"
        }
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    fun traceError(logger: KLogger, method: String, starts: Instant, t: Throwable) {
        logger.error(t) { "An error occurred in $method after ${Duration.between(starts, Instant.now())}" }
    }

    fun <T> toNullable(optional: Optional<T>): T? =
        if (optional.isPresent) optional.get()
        else null
}
