package com.killrvideo.service.user.grpc;

import com.killrvideo.utils.FluentValidator;
import io.grpc.stub.StreamObserver;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Validate GRPC parameters.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
public class UserManagementServiceGrpcValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementServiceGrpcValidator.class);

    /**
     * Validate create user.
     *
     * @param request        request.
     * @param streamObserver response.
     */
    public void validateGrpcRequest_createUser(CreateUserRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("createUser", request, LOGGER, streamObserver)
                .notEmpty("user id", isBlank(request.getUserId().getValue()))
                .notEmpty("password", isBlank(request.getPassword()))
                .notEmpty("email", isBlank(request.getEmail()))
                .validate();
    }

    public void validateGrpcRequest_VerifyCredentials(VerifyCredentialsRequest request, StreamObserver<?> streamObserver) {
        FluentValidator.of("verifyCredentials", request, LOGGER, streamObserver)
                .notEmpty("email", isBlank(request.getEmail()))
                .notEmpty("password", isBlank(request.getPassword()))
                .validate();
    }

    public void validateGrpcRequest_getUserProfile(GetUserProfileRequest request, StreamObserver<?> streamObserver) {
        FluentValidator validator = FluentValidator.of("verifyCredentials", request, LOGGER, streamObserver)
                .error("cannot get more than 20 user profiles at once for get user profile request",
                        request.getUserIdsCount() > 20);
        request.getUserIdsList().forEach(uuid ->
                validator.error("provided UUID values cannot be null or blank for get user profile request",
                        uuid == null || isBlank(uuid.getValue()))
        );
        validator.validate();
    }
}
