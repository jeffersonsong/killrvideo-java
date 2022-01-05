package com.killrvideo.utils

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import org.apache.commons.lang3.exception.ExceptionUtils
import java.time.Duration
import java.time.Instant

object FormatUtils {
    private val PATTERN = "\\s+".toRegex()
    private const val BASE_PACKAGE = "com.killrvideo"

    fun format(message: Any?): String =
        when(message) {
            null -> "Void"
            is Message -> JsonFormat.printer().print(message).replace(PATTERN, " ")
            else -> message.toString()
        }

    fun formatElapse(starts: Instant): String {
        val elapse = Duration.between(starts, Instant.now()).nano / 1000
        return "%,d ms".format(elapse)
    }

    fun formatException(ex: Throwable): String {
        val stacktrace = ExceptionUtils.getStackTrace(ex).split("\n")
        return stacktrace.filter {it.contains(BASE_PACKAGE)}.joinToString("\\n")
    }
}
