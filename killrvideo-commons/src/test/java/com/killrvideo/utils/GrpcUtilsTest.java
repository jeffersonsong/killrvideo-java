package com.killrvideo.utils;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import static com.killrvideo.utils.GrpcUtils.returnSingleResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcUtilsTest {

    @Test
    public void testReturnSingleResult() {
        String result = "hello";
        StreamObserver<String> grpcResObserver = mock(StreamObserver.class);
        returnSingleResult(result, grpcResObserver);
        verify(grpcResObserver, times(1)).onNext(anyString());
        verify(grpcResObserver, times(1)).onCompleted();
    }
}
