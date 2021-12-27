package com.killrvideo.service.user.grpc;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest;

import static com.killrvideo.utils.ValidationUtils.initErrorString;
import static com.killrvideo.utils.ValidationUtils.validate;

/**
 * Validate GRPC parameters.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
public class UserManagementServiceGrpcValidator {
    private static Logger LOGGER = LoggerFactory.getLogger(UserManagementServiceGrpcValidator.class);

    /**
     * Validate create user.
     *
     * @param request
     * @param streamObserver
     */
    public void validateGrpcRequest_createUser(CreateUserRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (request.getUserId() == null || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for create user request\n");
            isValid = false;
        }
        if (isBlank(request.getPassword())) {
            errorMessage.append("\t\tpassword should be provided for create user request\n");
            isValid = false;
        }
        if (isBlank(request.getEmail())) {
            errorMessage.append("\t\temail should be provided for create user request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'createUser'");
    }
    
    public void validateGrpcRequest_VerifyCredentials(VerifyCredentialsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (isBlank(request.getEmail())) {
            errorMessage.append("\t\temail should be provided for verify credentials request\n");
            isValid = false;
        }

        if (isBlank(request.getPassword())) {
            errorMessage.append("\t\tpassword should be provided for verify credentials request\n");
            isValid = false;
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'verifyCredentials'");
    }
    
    public void validateGrpcRequest_getUserProfile(GetUserProfileRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;
        if (request.getUserIdsCount() > 20) {
            errorMessage.append("\t\tcannot get more than 20 user profiles at once for get user profile request\n");
            isValid = false;
        }
        for (CommonTypes.Uuid uuid : request.getUserIdsList()) {
            if (uuid == null || isBlank(uuid.getValue())) {
                errorMessage.append("\t\tprovided UUID values cannot be null or blank for get user profile request\n");
                isValid = false;
            }
        }
        Assert.isTrue(validate(LOGGER, streamObserver, errorMessage, isValid), "Invalid parameter for 'getUserProfile'");
    }

}
