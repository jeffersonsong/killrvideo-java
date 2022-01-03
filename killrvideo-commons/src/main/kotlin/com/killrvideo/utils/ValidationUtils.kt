package com.killrvideo.utils

import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.Logger

object ValidationUtils {
    /**
     * Init error builder.
     *
     * @param request
     * current request
     * @return
     * current error message
     */
    fun initErrorString(request: Any): StringBuilder =
        StringBuilder("Validation error for '$request' : \n")

    /**
     * Deduplicate condition evaluation.
     *
     * @param assertion
     * current condition
     * @param fieldName
     * fieldName to evaluate
     * @param request
     * GRPC reauest
     * @param errorMessage
     * concatenation of error messages
     * @return
     */
    fun notEmpty(assertion: Boolean, fieldName: String?, request: String?, errorMessage: StringBuilder): Boolean {
        if (assertion) {
            errorMessage.append("\t\t")
            errorMessage.append(fieldName)
            errorMessage.append("should be provided for ")
            errorMessage.append(request)
            errorMessage.append("\n")
        }
        return !assertion
    }

    /**
     * Add error message if assertion is violated.
     *
     * @param assertion
     * current assertion
     * @param fieldName
     * current field name
     * @param request
     * current request
     * @param errorMessage
     * current error message
     * @return
     * if the correction is OK.
     */
    fun positive(assertion: Boolean, fieldName: String?, request: String?, errorMessage: StringBuilder): Boolean {
        if (assertion) {
            errorMessage.append("\t\t")
            errorMessage.append(fieldName)
            errorMessage.append("should be strictly positive for ")
            errorMessage.append(request)
            errorMessage.append("\n")
        }
        return !assertion
    }

    /**
     * Utility to validate Grpc Input.
     *
     * @param streamObserver
     * grpc observer
     * @param errorMessage
     * error mressage
     * @param isValid
     * validation of that
     * @return
     * ok
     */
    fun validate(
        logger: Logger,
        streamObserver: StreamObserver<*>,
        errorMessage: StringBuilder,
        isValid: Boolean
    ): Boolean =
        if (isValid) {
            true
        } else {
            val description = errorMessage.toString()
            logger.error(description)
            streamObserver.onError(Status.INVALID_ARGUMENT.withDescription(description).asRuntimeException())
            streamObserver.onCompleted()
            false
        }
}