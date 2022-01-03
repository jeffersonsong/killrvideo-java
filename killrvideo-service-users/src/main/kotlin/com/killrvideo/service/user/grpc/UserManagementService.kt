package com.killrvideo.service.user.grpc

import com.killrvideo.service.user.dto.User
import com.killrvideo.service.user.dto.UserCredentials
import com.killrvideo.service.user.repository.UserRepository
import com.killrvideo.utils.HashUtils
import io.grpc.Status
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserManagementService(
    private val userRepository: UserRepository
) {
    suspend fun createUser(user: User, hashedPassword: String): Result<Boolean> =
        runCatching { userRepository.createUserAsync(user, hashedPassword) }

    suspend fun verifyCredentials(email: String, password: String): Result<UserCredentials> {
        val result = runCatching {
            userRepository.getUserCredentialAsync(email)
        }

        return if (result.isSuccess) {
            val credential = result.getOrNull()
            if (credential == null || credential.password == null || !HashUtils.isPasswordValid(password, credential.password!!)) {
                val error = Status.INVALID_ARGUMENT
                    .withDescription("Email address or password are not correct").asRuntimeException()
                Result.failure(error)
            } else {
                Result.success(credential)
            }
        } else {
            Result.failure(result.exceptionOrNull()!!)
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