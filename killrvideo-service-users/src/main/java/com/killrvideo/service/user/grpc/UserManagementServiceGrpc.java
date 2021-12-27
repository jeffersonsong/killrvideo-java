package com.killrvideo.service.user.grpc;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.killrvideo.service.user.repository.UserRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.protobuf.Timestamp;
import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.user.dto.User;
import com.killrvideo.service.user.dto.UserCredentials;
import com.killrvideo.utils.HashUtils;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.user_management.UserManagementServiceGrpc.UserManagementServiceImplBase;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse;
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse;
import killrvideo.user_management.events.UserManagementEvents.UserCreated;

import javax.inject.Inject;

/**
 * Create or update users.
 *
 * @author DataStax advocates Team
 */
@Service("killrvideo.service.grpc.user")
public class UserManagementServiceGrpc extends UserManagementServiceImplBase {
    
    /** Loger for that class. */
    private static Logger LOGGER = LoggerFactory.getLogger(UserManagementServiceGrpc.class);
    
    @Value("${killrvideo.messaging.destinations.userCreated : topic-kv-userCreation}")
    private String topicUserCreated;
    
    @Value("${killrvideo.discovery.services.user : UserManagementService}")
    private String serviceKey;
    
    @Inject
    private UserRepository userRepository;

    @Inject
    private MessagingDao messagingDao;

    @Inject
    private UserManagementServiceGrpcValidator validator;
    @Inject
    private UserManagementServiceGrpcMapper mapper;
    
     /** {@inheritDoc} */
    @Override
    public void createUser(
            final CreateUserRequest grpcReq, 
            final StreamObserver<CreateUserResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_createUser(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // Mapping GRPC => Domain (Dao)
        User user = mapper.mapUserRequest2User(grpcReq);
        final String hashedPassword = HashUtils.hashPassword(grpcReq.getPassword().trim());
        
         // Invoke DAO Async
        userRepository.createUserAsync(user, hashedPassword).whenComplete((result, error) -> {
            if (error != null ) {
                traceError("createUser", starts, error);
                grpcResObserver.onError(Status.INVALID_ARGUMENT.augmentDescription(error.getMessage())
                               .asRuntimeException());
            } else {
                traceSuccess("createUser", starts);
                messagingDao.sendEvent(topicUserCreated, mapper.createUserCreatedEvent(result));
                grpcResObserver.onNext(CreateUserResponse.newBuilder().build());
                grpcResObserver.onCompleted();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void verifyCredentials(
            final VerifyCredentialsRequest grpcReq, 
            final StreamObserver<VerifyCredentialsResponse> grpcResObserver) {
        
        // Validate Parameters
        validator.validateGrpcRequest_VerifyCredentials(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // Mapping GRPC => Domain (Dao)
        String email = grpcReq.getEmail();
        
        // Invoke Async
        CompletableFuture<UserCredentials> futureCredential = userRepository.getUserCredentialAsync(email);
        
        // Map back as GRPC (if correct invalid credential otherwize)
        futureCredential.whenComplete((credential, error) -> {
            if (error != null ) {
                traceError("verifyCredentials", starts, error);
                if (!HashUtils.isPasswordValid(grpcReq.getPassword(), credential.getPassword())) {
                    grpcResObserver.onError(Status.INVALID_ARGUMENT
                                   .withDescription("Email address or password are not correct").asRuntimeException());
                } else {
                    grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                }
            } else {
                traceSuccess("verifyCredentials", starts);
                grpcResObserver.onNext(mapper.mapResponseVerifyCredentials(credential.getUserid()));
                grpcResObserver.onCompleted();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void getUserProfile(
            final GetUserProfileRequest grpcReq, 
            final StreamObserver<GetUserProfileResponse> grpcResObserver) {
        
        // Validate Parameters
        validator.validateGrpcRequest_getUserProfile(grpcReq, grpcResObserver);
        
        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();
        
        // No user id provided, still a valid confition
        final GetUserProfileResponse.Builder builder = GetUserProfileResponse.newBuilder();
        if (grpcReq.getUserIdsCount() == 0 || CollectionUtils.isEmpty(grpcReq.getUserIdsList())) {
            traceSuccess("getUserProfile", starts);
            grpcResObserver.onNext(builder.build());
            grpcResObserver.onCompleted();
            LOGGER.debug("No user id provided");
        } else {
            
            // Mapping GRPC => Domain (Dao)
            List < UUID > listOfUserId = Arrays.asList(grpcReq
                    .getUserIdsList()
                    .stream()
                    .map(uuid -> UUID.fromString(uuid.getValue()))
                    .toArray(UUID[]::new));
            
            // Execute Async
            CompletableFuture<List<User>> userListFuture = userRepository.getUserProfilesAsync(listOfUserId);
            
            // Mapping back to GRPC objects
            userListFuture.whenComplete((users, error) -> {
                if (error != null ) {
                    traceError("getUserProfile", starts, error);
                    grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                } else {
                    traceSuccess("getUserProfile", starts);
                    users.stream()
                         .map(mapper::mapUserToGrpcUserProfile)
                         .forEach(builder::addProfiles);    
                    grpcResObserver.onNext(builder.build());
                    grpcResObserver.onCompleted();
                }
            });
        }
    }
    
    /**
     * Utility to TRACE.
     *
     * @param method
     *      current operation
     * @param starts
     *      timestamp for starting
     */
    private void traceSuccess(String method, Instant starts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("End successfully '{}' in {} millis", method, Duration.between(starts, Instant.now()).getNano()/1000);
        }
    }
    
    /**
     * Utility to TRACE.
     *
     * @param method
     *      current operation
     * @param starts
     *      timestamp for starting
     */
    private void traceError(String method, Instant starts, Throwable t) {
        LOGGER.error("An error occured in {} after {}", method, Duration.between(starts, Instant.now()), t);
    }

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return
     *       current value of 'serviceKey'
     */
    public String getServiceKey() {
        return serviceKey;
    }
}
