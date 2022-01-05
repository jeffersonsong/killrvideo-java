package com.killrvideo.utils

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat

object FormatUtils {
    private val PATTERN = "\\s+".toRegex()

    fun format(message: Any?): String =
        when(message) {
            null -> "Void"
            is Message -> JsonFormat.printer().print(message).replace(PATTERN, " ")
            else -> message.toString()
        }
}
