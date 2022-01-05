package com.killrvideo.grpc.interceptor

import com.killrvideo.utils.FormatUtils
import io.grpc.*
import mu.KotlinLogging

class LogRequestResponseInterceptor : ServerInterceptor {
    private val logger = KotlinLogging.logger {}

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val method = call.methodDescriptor.bareMethodName

        val wrappedCall = object: ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun sendMessage(message: RespT) {
                logger.debug { "Method: $method, Response: ${FormatUtils.format(message)}" }
                super.sendMessage(message)
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
}
