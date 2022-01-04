package com.killrvideo.service.user.grpc

import com.killrvideo.service.user.dto.User
import com.killrvideo.service.user.dto.UserCredentials
import com.killrvideo.service.user.repository.UserRepository
import com.killrvideo.utils.HashUtils
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserManagementService(
    private val userRepository: UserRepository
) {
    suspend fun createUser(user: User, hashedPassword: String): Result<Boolean> =
        runCatching { userRepository.createUserAsync(user, hashedPassword) }

    suspend fun verifyCredentials(email: String, password: String): Result<UserCredentials> {
        return runCatching {
            userRepository.getUserCredentialAsync(email)
        }.mapCatching { credential ->
            if (credential?.password == null || !HashUtils.isPasswordValid(password, credential.password!!)) {
                throw IllegalArgumentException("Email address or password are not correct")
            }
            credential
        }
    }

    suspend fun getUserProfile(listOfUserId: List<UUID>): Result<List<User>> {
        // No user id provided, still a valid condition
        return if (listOfUserId.isEmpty()) {
            Result.success(emptyList())
        } else {
            runCatching {
                userRepository.getUserProfilesAsync(listOfUserId)
            }
        }
    }
}