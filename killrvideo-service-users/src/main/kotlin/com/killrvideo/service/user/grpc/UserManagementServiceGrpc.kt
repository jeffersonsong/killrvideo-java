package com.killrvideo.service.user.grpc

import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.user.grpc.UserManagementServiceGrpcMapper.CreateUserRequestExtensions.parse
import com.killrvideo.service.user.grpc.UserManagementServiceGrpcMapper.GetUserProfileRequestExtensions.parse
import com.killrvideo.service.utils.ServiceGrpcUtils.trace
import killrvideo.user_management.UserManagementServiceGrpcKt
import killrvideo.user_management.UserManagementServiceOuterClass.*
import killrvideo.user_management.createUserResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Create or update users.
 *
 * @author DataStax advocates Team
 */
@Service("killrvideo.service.grpc.user")
class UserManagementServiceGrpc(
    private val userService: UserManagementService,
    private val messagingDao: MessagingDao,
    private val validator: UserManagementServiceGrpcValidator,
    private val mapper: UserManagementServiceGrpcMapper
) : UserManagementServiceGrpcKt.UserManagementServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    @Value("\${killrvideo.messaging.destinations.userCreated : topic-kv-userCreation}")
    private val topicUserCreated: String? = null

    /**
     * Getter accessor for attribute 'serviceKey'.
     *
     * @return current value of 'serviceKey'
     */
    @Value("\${killrvideo.discovery.services.user : UserManagementService}")
    val serviceKey: String? = null

    override suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
        // Validate Parameters
        validator.validateGrpcRequest_createUser(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val (user, hashedPassword) = request.parse()

        // Invoke DAO Async
        val result: Result<CreateUserResponse> = userService.createUser(user, hashedPassword)
            .map { createUserResponse {} }

        return result
            .onSuccess { messagingDao.sendEvent(topicUserCreated, mapper.createUserCreatedEvent(user))}
            .trace(logger, "createUser", starts).getOrThrow()
    }

    override suspend fun verifyCredentials(request: VerifyCredentialsRequest): VerifyCredentialsResponse {
        // Validate Parameters
        validator.validateGrpcRequest_VerifyCredentials(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        return userService.verifyCredentials(request.email, request.password)
            .map { credential -> mapper.mapResponseVerifyCredentials(credential.userid) }
            .trace(logger, "verifyCredentials", starts)
            .getOrThrow()
    }

    override suspend fun getUserProfile(request: GetUserProfileRequest): GetUserProfileResponse {
        // Validate Parameters
        validator.validateGrpcRequest_getUserProfile(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        // Mapping GRPC => Domain (Dao)
        val listOfUserId = request.parse()

        // Execute Async
        return userService.getUserProfile(listOfUserId)
            .map { mapper.buildGetUserProfileResponse(it) }
            .trace(logger, "getUserProfile", starts)
            .getOrThrow()
    }
}
