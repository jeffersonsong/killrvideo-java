package com.killrvideo.service.utils

import io.grpc.*
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant

class TraceServiceCallInterceptor : ServerInterceptor {
    private val logger = KotlinLogging.logger {}

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val starts = Instant.now()
        val method = call.methodDescriptor.bareMethodName

        val wrapped = object: ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun close(status: Status, trailers: Metadata) {
                if (status.isOk) {
                    logger.debug {
                        "End successfully '$method' in ${Duration.between(starts, Instant.now()).nano / 1000} millis"
                    }
                } else {
                    logger.error(status.cause) {
                        "An error occurred in $method after ${Duration.between(starts, Instant.now())}"
                    }
                }
                return super.close(status, trailers)
            }
        }
        return next.startCall(wrapped, headers)
    }
}
