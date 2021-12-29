package com.killrvideo.utils;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;

public class FluentValidator {

    private final Logger logger;
    private final StreamObserver<?> streamObserver;
    private final String method;
    private final String requestName;
    private final StringBuilder errorMessage;
    private boolean isValid;

    public static FluentValidator of(String method, Object request,  Logger logger, StreamObserver<?> streamObserver) {
        return new FluentValidator(request, method, logger, streamObserver);
    }

    private FluentValidator(Object request, String method, Logger logger, StreamObserver<?> streamObserver) {
        this.errorMessage = ValidationUtils.initErrorString(request);
        this.requestName = method + " request";
        this.method = method;
        this.logger = logger;
        this.streamObserver = streamObserver;
        this.isValid = true;
    }

    public FluentValidator error(String message, boolean assertion) {
        if (this.isValid && assertion) {
            errorMessage.append("\t\t")
                        .append(message)
                        .append("\n");
            this.isValid = false;
        }
        return this;
    }

    public FluentValidator notEmpty(String fieldName, boolean assertion) {
        return error(String.format("%s should be provided for %s", fieldName, this.requestName), assertion);
    }

    public FluentValidator positive(String fieldName, boolean assertion) {
        return error(String.format("%s should be strictly positive for %s", fieldName, this.requestName), assertion);
    }

    public FluentValidator validate() {
        if (!isValid) {
            final String description = errorMessage.toString();
            logger.error(description);
            streamObserver.onError(Status.INVALID_ARGUMENT.withDescription(description).asRuntimeException());
            streamObserver.onCompleted();
            throw new IllegalArgumentException(String.format("Invalid parameter for '%s'", method));
        }
        return this;
    }
}
