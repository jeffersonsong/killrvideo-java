package com.killrvideo.utils;

import io.grpc.stub.StreamObserver;

public class GrpcUtils {
    private GrpcUtils(){}

    public static <T> void returnSingleResult(T result, StreamObserver<T> grpcResObserver) {
        grpcResObserver.onNext(result);
        grpcResObserver.onCompleted();
    }
}
