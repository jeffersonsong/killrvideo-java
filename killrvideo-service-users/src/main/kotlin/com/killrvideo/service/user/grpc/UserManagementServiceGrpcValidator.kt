package com.killrvideo.service.user.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.user_management.UserManagementServiceOuterClass.*
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils.isBlank
import org.springframework.stereotype.Component

/**
 * Validate GRPC parameters.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
open class UserManagementServiceGrpcValidator {
    private val logger = KotlinLogging.logger { }
    /**
     * Validate create user.
     *
     * @param request        request.
     */
    fun validateGrpcRequest_createUser(request: CreateUserRequest) {
        FluentValidator.of("createUser", request, logger)
            .notEmpty("user id", isBlank(request.userId.value))
            .notEmpty("password", isBlank(request.password))
            .notEmpty("email", isBlank(request.email))
            .validate()
    }

    fun validateGrpcRequest_VerifyCredentials(request: VerifyCredentialsRequest) {
        FluentValidator.of("verifyCredentials", request, logger)
            .notEmpty("email", isBlank(request.email))
            .notEmpty("password", isBlank(request.password))
            .validate()
    }

    fun validateGrpcRequest_getUserProfile(request: GetUserProfileRequest) {
        val validator: FluentValidator = FluentValidator.of("verifyCredentials", request, logger)
            .error(
                "cannot get more than 20 user profiles at once for get user profile request",
                request.userIdsCount > 20
            )
        request.userIdsList.forEach { uuid ->
            validator.error(
                "provided UUID values cannot be null or blank for get user profile request",
                uuid == null || isBlank(uuid.value)
            )
        }
        validator.validate()
    }
}
