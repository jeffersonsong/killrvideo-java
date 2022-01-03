package com.killrvideo.service.user.grpc

import com.google.protobuf.Timestamp
import com.killrvideo.service.user.dto.User
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import com.killrvideo.utils.HashUtils
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest
import killrvideo.user_management.events.userCreated
import killrvideo.user_management.getUserProfileResponse
import killrvideo.user_management.userProfile
import killrvideo.user_management.verifyCredentialsResponse
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * Mapping from interfaces GRPC to DTO
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
open class UserManagementServiceGrpcMapper {
    object CreateUserRequestExtensions {
        fun CreateUserRequest.parse(): Pair<User, String> =
            Pair(
                User(
                    userid = fromUuid(this.userId),
                    email = this.email,
                    firstname = this.firstName,
                    lastname = this.lastName,
                    createdDate = Instant.now()
                ),
                HashUtils.hashPassword(this.password.trim())
            )
    }

    object GetUserProfileRequestExtensions {
        fun GetUserProfileRequest.parse(): List<UUID> =
            this.userIdsList
                .filter { isNotBlank(it.value) }
                .map { fromUuid(it) }
    }

    fun mapResponseVerifyCredentials(userid: UUID?) =
        verifyCredentialsResponse {
            userid?.let { userId = uuidToUuid(it) }
        }

    fun createUserCreatedEvent(user: User) =
        userCreated {
            user.email?.let {email = it}
            user.firstname?.let {firstName = it}
            user.lastname?.let {lastName = it}
            user.userid?.let {userId = uuidToUuid(it)}
            timestamp = Timestamp.newBuilder().build()
        }

    fun buildGetUserProfileResponse(users: List<User>) =
        getUserProfileResponse {
            profiles.addAll(users.map { mapUserToGrpcUserProfile(it) })
        }

    private fun mapUserToGrpcUserProfile(user: User) =
        userProfile {
            user.email?.let { email = it }
            user.firstname?.let { firstName = it }
            user.lastname?.let { lastName = it }
            user.userid?.let { userId = uuidToUuid(it) }
        }
}
