package com.killrvideo.messaging.utils

import com.google.protobuf.Timestamp
import killrvideo.common.CommonEvents.ErrorEvent
import java.util.*

/**
 * Utility class for messaging
 *
 * @author Cedrick LUNVEN (@clunven)
 */
object MessagingUtils {
    /**
     * Creation GRPC CUSTOMN EXCEPTION
     *
     * @param t
     * grpc exception
     * @return
     */
    @JvmStatic
    fun mapError(t: Throwable): ErrorEvent =
        ErrorEvent.newBuilder()
            .setErrorMessage(t.message)
            .setErrorClassname(t.javaClass.name)
            .setErrorStack(mergeStackTrace(t))
            .setErrorTimestamp(Timestamp.newBuilder())
            .build()

    @JvmStatic
    fun mapCustomError(customError: String): ErrorEvent =
        ErrorEvent.newBuilder()
            .setErrorMessage(customError)
            .setErrorClassname(Exception::class.java.name)
            .setErrorTimestamp(Timestamp.newBuilder())
            .build()

    /**
     * Dump a stacktrace in a String,
     *
     * @param throwable
     * current exception raised by the program
     * @return
     * merged stack trace.
     */
    private fun mergeStackTrace(throwable: Throwable): String {
        val joiner = StringJoiner("\n\t", "\n", "\n")
        joiner.add(throwable.message)
        listOf(*throwable.stackTrace).forEach { joiner.add(it.toString()) }
        return joiner.toString()
    }
}
