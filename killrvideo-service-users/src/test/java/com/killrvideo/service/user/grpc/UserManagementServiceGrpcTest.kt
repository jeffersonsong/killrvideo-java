package com.killrvideo.service.user.grpc;

import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.user.dto.User;
import com.killrvideo.service.user.dto.UserCredentials;
import com.killrvideo.service.user.repository.UserRepository;
import com.killrvideo.utils.GrpcMappingUtils;
import io.grpc.stub.StreamObserver;
import killrvideo.user_management.UserManagementServiceOuterClass.*;
import killrvideo.user_management.events.UserManagementEvents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.killrvideo.utils.HashUtils.hashPassword;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class UserManagementServiceGrpcTest {
    @InjectMocks private UserManagementServiceGrpc service;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessagingDao messagingDao;
    @Mock
    private UserManagementServiceGrpcValidator validator;
    @Mock
    private UserManagementServiceGrpcMapper mapper;
    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testCreateUserWithValidationFailure() {
        CreateUserRequest grpcReq = CreateUserRequest.getDefaultInstance();
        StreamObserver<CreateUserResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_createUser(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.createUser(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testCreateUserWithCreateFailure() {
        CreateUserRequest grpcReq = createUserRequest("passwd", "joe@gmail.com");
        StreamObserver<CreateUserResponse> grpcResObserver = mock(StreamObserver.class);

        User user = mock(User.class);
        when(this.mapper.mapUserRequest2User(grpcReq)).thenReturn(user);

        doNothing().when(this.validator).validateGrpcRequest_createUser(any(), any());
        when(userRepository.createUserAsync(any(), any())).thenReturn(
            CompletableFuture.failedFuture(new Exception())
        );

        service.createUser(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testCreateUser() {
        CreateUserRequest grpcReq = createUserRequest("passwd", "joe@gmail.com");
        StreamObserver<CreateUserResponse> grpcResObserver = mock(StreamObserver.class);

        User user = mock(User.class);
        when(this.mapper.mapUserRequest2User(grpcReq)).thenReturn(user);

        UserManagementEvents.UserCreated event = UserManagementEvents.UserCreated.getDefaultInstance();
        when(this.mapper.createUserCreatedEvent(user)).thenReturn(event);

        doNothing().when(this.validator).validateGrpcRequest_createUser(any(), any());
        when(userRepository.createUserAsync(any(), any())).thenReturn(
                CompletableFuture.completedFuture(null)
        );
        when(this.messagingDao.sendEvent(any(), any())).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        service.createUser(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testVerifyCredentialsWithValidationFailure() {
        VerifyCredentialsRequest grpcReq = VerifyCredentialsRequest.getDefaultInstance();
        StreamObserver<VerifyCredentialsResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_VerifyCredentials(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.verifyCredentials(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testVerifyCredentialsWithQueryFailure() {
        VerifyCredentialsRequest grpcReq = verifyCredentialsRequest("joe@gmail.com", "passwd");
        StreamObserver<VerifyCredentialsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_VerifyCredentials(any(), any());

        when(userRepository.getUserCredentialAsync(any())).thenReturn(
          CompletableFuture.failedFuture(new Exception())
        );

        service.verifyCredentials(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testVerifyCredentialsWithPasswdMismatch() {
        VerifyCredentialsRequest grpcReq = verifyCredentialsRequest("joe@gmail.com", "passwd");
        StreamObserver<VerifyCredentialsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_VerifyCredentials(any(), any());

        UserCredentials credentials = userCredentials("joe@gmail.com", "xxx");
        when(userRepository.getUserCredentialAsync(any())).thenReturn(
                CompletableFuture.completedFuture(credentials)
        );

        service.verifyCredentials(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testVerifyCredentials() {
        VerifyCredentialsRequest grpcReq = verifyCredentialsRequest("joe@gmail.com", "passwd");
        StreamObserver<VerifyCredentialsResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_VerifyCredentials(any(), any());

        UserCredentials credentials = userCredentials("joe@gmail.com", hashPassword("passwd"));
        when(userRepository.getUserCredentialAsync(any())).thenReturn(
                CompletableFuture.completedFuture(credentials)
        );

        service.verifyCredentials(grpcReq, grpcResObserver);
        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetUserProfileWithValidationFailure() {
        GetUserProfileRequest grpcReq = GetUserProfileRequest.getDefaultInstance();
        StreamObserver<GetUserProfileResponse> grpcResObserver = mock(StreamObserver.class);

        doThrow(new IllegalArgumentException()).when(this.validator)
                .validateGrpcRequest_getUserProfile(any(), any());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.getUserProfile(grpcReq, grpcResObserver)
        );
    }

    @Test
    void testGetUserProfileWithEmptyUserIds() {
        GetUserProfileRequest grpcReq = getUserProfileRequest();
        StreamObserver<GetUserProfileResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getUserProfile(any(), any());
        service.getUserProfile(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @Test
    void testGetUserProfileWithQueryFailure() {
        UUID userId = UUID.randomUUID();
        GetUserProfileRequest grpcReq = getUserProfileRequest(userId);
        StreamObserver<GetUserProfileResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getUserProfile(any(), any());
        when(this.mapper.mapToListOfUserid(any())).thenReturn(singletonList(userId));

        when(this.userRepository.getUserProfilesAsync(any())).thenReturn(
          CompletableFuture.failedFuture(new Exception())
        );
        service.getUserProfile(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(1)).onError(any());
        verify(grpcResObserver, times(0)).onNext(any());
        verify(grpcResObserver, times(0)).onCompleted();
    }

    @Test
    void testGetUserProfile() {
        UUID userId = UUID.randomUUID();
        GetUserProfileRequest grpcReq = getUserProfileRequest(userId);
        StreamObserver<GetUserProfileResponse> grpcResObserver = mock(StreamObserver.class);

        doNothing().when(this.validator).validateGrpcRequest_getUserProfile(any(), any());
        when(this.mapper.mapToListOfUserid(any())).thenReturn(singletonList(userId));

        User user = mock(User.class);
        List<User> users = singletonList(user);

        GetUserProfileResponse response = GetUserProfileResponse.getDefaultInstance();
        when(this.mapper.buildGetUserProfileResponse(users)).thenReturn(response);

        when(this.userRepository.getUserProfilesAsync(any())).thenReturn(
                CompletableFuture.completedFuture(users)
        );
        service.getUserProfile(grpcReq, grpcResObserver);

        verify(grpcResObserver, times(0)).onError(any());
        verify(grpcResObserver, times(1)).onNext(any());
        verify(grpcResObserver, times(1)).onCompleted();
    }

    @SuppressWarnings("SameParameterValue")
    private CreateUserRequest createUserRequest(String passwd, String email) {
        return CreateUserRequest.newBuilder()
                .setPassword(passwd)
                .setEmail(email)
                .build();
    }

    @SuppressWarnings("SameParameterValue")
    private VerifyCredentialsRequest verifyCredentialsRequest(String email, String passwd) {
        return VerifyCredentialsRequest.newBuilder()
                .setEmail(email)
                .setPassword(passwd)
                .build();
    }

    @SuppressWarnings("SameParameterValue")
    private UserCredentials userCredentials(String email, String passwd) {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn(email);
        return UserCredentials.from(user, passwd);
    }

    private GetUserProfileRequest getUserProfileRequest(UUID... userIds) {
        return GetUserProfileRequest.newBuilder()
                .addAllUserIds(
                        Arrays.stream(userIds).map(GrpcMappingUtils::uuidToUuid)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
