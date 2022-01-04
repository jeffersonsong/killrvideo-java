package com.killrvideo.service.user.grpc

import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.user.dto.User
import com.killrvideo.service.user.grpc.UserManagementServiceGrpcMapper.CreateUserRequestExtensions.parse
import com.killrvideo.service.user.grpc.UserManagementServiceGrpcMapper.GetUserProfileRequestExtensions.parse
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
    private val mapper: UserManagementServiceGrpcMapper,
    @Value("\${killrvideo.messaging.destinations.userCreated : topic-kv-userCreation}")
    private val topicUserCreated: String,
    @Value("\${killrvideo.discovery.services.user : UserManagementService}")
    val serviceKey: String
) : UserManagementServiceGrpcKt.UserManagementServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    override suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
        // Validate Parameters
        validator.validateGrpcRequest_createUser(request)

        // Mapping GRPC => Domain (Dao)
        val (user, hashedPassword) = request.parse()

        // Invoke DAO Async
        val result: Result<CreateUserResponse> = userService.createUser(user, hashedPassword)
            .map { createUserResponse {} }

        return result
            .onSuccess { notifyUserCreated(user) }
            .getOrThrow()
    }

    private fun notifyUserCreated(user: User) =
        messagingDao.sendEvent(topicUserCreated, mapper.createUserCreatedEvent(user))

    override suspend fun verifyCredentials(request: VerifyCredentialsRequest): VerifyCredentialsResponse {
        // Validate Parameters
        validator.validateGrpcRequest_VerifyCredentials(request)

        // Stands as stopwatch for logging and messaging 
        val starts = Instant.now()

        return userService.verifyCredentials(request.email, request.password)
            .map { credential -> mapper.mapResponseVerifyCredentials(credential.userid) }
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
            .getOrThrow()
    }
}
