package com.killrvideo.utils

import org.slf4j.Logger

class FluentValidator private constructor(
    request: Any,
    method: String,
    private val logger: Logger
) {
    private val requestName: String = "$method request"
    private val errorMessage: StringBuilder = StringBuilder("Validation error for '$request' : \n")
    private var isValid: Boolean = true

    fun error(message: String, assertion: Boolean): FluentValidator {
        if (isValid && assertion) {
            errorMessage.append("\t\t")
                .append(message)
                .append("\n")
            isValid = false
        }
        return this
    }

    fun notEmpty(fieldName: String, assertion: Boolean): FluentValidator =
        error("$fieldName should be provided for $requestName", assertion)

    fun positive(fieldName: String, assertion: Boolean): FluentValidator =
        error("$fieldName should be strictly positive for $requestName", assertion)

    fun validate() {
        if (!isValid) {
            val description = errorMessage.toString()
            logger.error(description)
            throw IllegalArgumentException(description)
        }
    }

    companion object {
        fun of(
            method: String,
            request: Any,
            logger: Logger
        ): FluentValidator {
            return FluentValidator(request, method, logger)
        }
    }
}
