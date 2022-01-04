package com.killrvideo.grpc.interceptor

import com.killrvideo.exception.AlreadyExistsException
import com.killrvideo.exception.NotFoundException
import io.grpc.*
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall

class GlobalGrpcExceptionHandler : ServerInterceptor {

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        val wrapped = object : SimpleForwardingServerCall<ReqT, RespT>(call) {
            /**
             * When closing a gRPC call, extract any error status information to top-level fields. Also
             * log the cause of errors.
             */
            override fun close(status: Status, trailers: Metadata) {
                val newStatus =
                    if (status.code == Status.Code.UNKNOWN) {
                        translate(status)
                    } else {
                        status
                    }

                return super.close(newStatus, trailers)
            }

            private fun translate(status: Status): Status {
                val cause = status.cause
                val translatedStatus = when (cause) {
                    is IllegalArgumentException -> Status.INVALID_ARGUMENT
                    is IllegalStateException -> Status.FAILED_PRECONDITION
                    is NotFoundException -> Status.NOT_FOUND
                    is AlreadyExistsException -> Status.ALREADY_EXISTS
                    else -> Status.UNKNOWN
                }
                return translatedStatus.withDescription(cause?.message).withCause(cause)
            }
        }

        return next.startCall(wrapped, headers)
    }
}
