package com.killrvideo.service.user.grpc;

import com.killrvideo.messaging.dao.MessagingDao;
import com.killrvideo.service.user.dto.User;
import com.killrvideo.service.user.repository.UserRepository;
import com.killrvideo.utils.HashUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.user_management.UserManagementServiceGrpc.UserManagementServiceImplBase;
import killrvideo.user_management.UserManagementServiceOuterClass.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.killrvideo.utils.GrpcUtils.returnSingleResult;

/**
 * Create or update users.
 *
 * @author DataStax advocates Team
 */
@Service("killrvideo.service.grpc.user")
public class UserManagementServiceGrpc extends UserManagementServiceImplBase {

    /**
     * Loger for that class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementServiceGrpc.class);

    @Value("${killrvideo.messaging.destinations.userCreated : topic-kv-userCreation}")
    private String topicUserCreated;

    @Value("${killrvideo.discovery.services.user : UserManagementService}")
    private String serviceKey;

    private final UserRepository userRepository;
    private final MessagingDao messagingDao;
    private final UserManagementServiceGrpcValidator validator;
    private final UserManagementServiceGrpcMapper mapper;

    public UserManagementServiceGrpc(UserRepository userRepository, MessagingDao messagingDao, UserManagementServiceGrpcValidator validator, UserManagementServiceGrpcMapper mapper) {
        this.userRepository = userRepository;
        this.messagingDao = messagingDao;
        this.validator = validator;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
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
        userRepository.createUserAsync(user, hashedPassword)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        traceError("createUser", starts, error);
                        grpcResObserver.onError(Status.INVALID_ARGUMENT.augmentDescription(error.getMessage())
                                .asRuntimeException());
                    } else {
                        traceSuccess("createUser", starts);
                        messagingDao.sendEvent(topicUserCreated, mapper.createUserCreatedEvent(user));
                        returnSingleResult(CreateUserResponse.newBuilder().build(), grpcResObserver);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
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
        userRepository.getUserCredentialAsync(email)
                .whenComplete((credential, error) -> {
                    // Map back as GRPC (if correct invalid credential otherwize)
                    if (error != null) {
                        traceError("verifyCredentials", starts, error);
                        grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                    } else if (!HashUtils.isPasswordValid(grpcReq.getPassword(), credential.getPassword())) {
                        grpcResObserver.onError(Status.INVALID_ARGUMENT
                                .withDescription("Email address or password are not correct").asRuntimeException());
                    } else {
                        traceSuccess("verifyCredentials", starts);
                        returnSingleResult(mapper.mapResponseVerifyCredentials(credential.getUserid()), grpcResObserver);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getUserProfile(
            final GetUserProfileRequest grpcReq,
            final StreamObserver<GetUserProfileResponse> grpcResObserver) {
        // Validate Parameters
        validator.validateGrpcRequest_getUserProfile(grpcReq, grpcResObserver);

        // Stands as stopwatch for logging and messaging 
        final Instant starts = Instant.now();

        // No user id provided, still a valid confition
        if (grpcReq.getUserIdsCount() == 0) {
            traceSuccess("getUserProfile", starts);
            returnSingleResult(GetUserProfileResponse.getDefaultInstance(), grpcResObserver);
            LOGGER.debug("No user id provided");

        } else {
            // Mapping GRPC => Domain (Dao)
            List<UUID> listOfUserId = mapper.mapToListOfUserid(grpcReq);

            // Execute Async
            userRepository.getUserProfilesAsync(listOfUserId)
                    .whenComplete((users, error) -> {
                        // Mapping back to GRPC objects
                        if (error != null) {
                            traceError("getUserProfile", starts, error);
                            grpcResObserver.onError(Status.INTERNAL.withCause(error).asRuntimeException());
                        } else {
                            traceSuccess("getUserProfile", starts);
                            returnSingleResult(mapper.buildGetUserProfileResponse(users), grpcResObserver);
                        }
                    });
        }
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    private void traceSuccess(String method, Instant starts) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("End successfully '{}' in {} millis", method, Duration.between(starts, Instant.now()).getNano() / 1000);
        }
    }

    /**
     * Utility to TRACE.
     *
     * @param method current operation
     * @param starts timestamp for starting
     */
    private void traceError(String method, Instant starts, Throwable t) {
        LOGGER.error("An error occured in {} after {}", method, Duration.between(starts, Instant.now()), t);
    }

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return current value of 'serviceKey'
     */
    public String getServiceKey() {
        return serviceKey;
    }
}
