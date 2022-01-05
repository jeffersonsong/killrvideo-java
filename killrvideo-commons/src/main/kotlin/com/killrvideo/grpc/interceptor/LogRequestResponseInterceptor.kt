package com.killrvideo.grpc.interceptor

import com.killrvideo.utils.FormatUtils
import io.grpc.*
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ExceptionUtils
import java.time.Duration
import java.time.Instant

class LogRequestResponseInterceptor : ServerInterceptor {
    private val logger = KotlinLogging.logger {}

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val starts = Instant.now()
        val method = call.methodDescriptor.fullMethodName

        val wrappedCall = object: ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun sendMessage(message: RespT) {
                logger.debug { "Method: $method, Elapse: ${formatElapse(starts)} ms, Response: ${FormatUtils.format(message)}" }
                super.sendMessage(message)
            }

            override fun close(status: Status, trailers: Metadata) {
                if (!status.isOk) {
                    if (status.cause != null) {
                        logger.error {
                            "Method: $method, Elapse: ${formatElapse(starts)}, Exception: ${formatException(status.cause!!)}"
                        }
                    } else {
                        logger.error {
                            "Method: $method, Elapse: ${formatElapse(starts)}, Error."
                        }
                    }
                }
                super.close(status, trailers)
            }
        }

        val listener = next.startCall(wrappedCall, headers)
        return object: ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            override fun onMessage(message: ReqT) {
                logger.debug {"Method: $method, Request: ${FormatUtils.format(message)}" }
                super.onMessage(message)
            }
        }
    }

    private fun formatElapse(starts: Instant): String {
        val elapse = Duration.between(starts, Instant.now()).nano / 1000
        return "%,d ms".format(elapse)
    }

    private fun formatException(ex: Throwable): String {
        val stacktrace = ExceptionUtils.getStackTrace(ex).split("\n")
        val builder = StringBuilder(stacktrace[0])

        for (i in IntRange(1, stacktrace.size)) {
            val line = stacktrace[i]
            if (line.contains(BASE_PACKAGE)) {
                builder.append("\\n").append(line)
            } else {
                break
            }
        }
        return builder.toString()
    }

    companion object {
        private const val BASE_PACKAGE = "com.killrvideo"
    }
}
