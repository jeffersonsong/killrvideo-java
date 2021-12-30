package com.killrvideo.service.user.grpc;

import io.grpc.stub.StreamObserver;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.killrvideo.utils.GrpcMappingUtils.randomUuid;
import static org.mockito.Mockito.*;

class UserManagementServiceGrpcValidatorTest {
    private final UserManagementServiceGrpcValidator validator = new UserManagementServiceGrpcValidator();
    private StreamObserver<?> streamObserver;

    @BeforeEach
    public void setUp() {
        this.streamObserver = mock(StreamObserver.class);
    }

    @Test
    public void testValidateGrpcRequest_createUser_Success() {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setUserId(randomUuid())
                .setPassword("passwd")
                .setEmail("joe@gmail.com")
                .build();

        validator.validateGrpcRequest_createUser(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_createUser_Failure() {
        CreateUserRequest request = CreateUserRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_createUser(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_VerifyCredentials_Success() {
        VerifyCredentialsRequest request = VerifyCredentialsRequest.newBuilder()
                .setPassword("passwd")
                .setEmail("joe@gmail.com")
                .build();

        validator.validateGrpcRequest_VerifyCredentials(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_VerifyCredentials_Failure() {
        VerifyCredentialsRequest request = VerifyCredentialsRequest.getDefaultInstance();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_VerifyCredentials(request, streamObserver)
        );

        verifyFailure();
    }

    @Test
    public void testValidateGrpcRequest_getUserProfile_Success() {
        GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
                .addUserIds(randomUuid())
                .build();

        validator.validateGrpcRequest_getUserProfile(request, streamObserver);

        verifySuccess();
    }

    @Test
    public void testValidateGrpcRequest_getUserProfile_Failure() {
        GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
                .addAllUserIds(
                        IntStream.range(0, 21).mapToObj(i -> randomUuid()).collect(Collectors.toList())
                )
                .build();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                validator.validateGrpcRequest_getUserProfile(request, streamObserver)
        );

        verifyFailure();
    }

    private void verifySuccess() {
        verify(streamObserver, times(0)).onError(any());
        verify(streamObserver, times(0)).onCompleted();
    }

    private void verifyFailure() {
        verify(streamObserver, times(1)).onError(any());
        verify(streamObserver, times(1)).onCompleted();
    }
}