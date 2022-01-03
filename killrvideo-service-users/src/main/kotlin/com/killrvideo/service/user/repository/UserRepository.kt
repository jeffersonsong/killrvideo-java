package com.killrvideo.service.user.repository

import com.killrvideo.dse.utils.MappedAsyncPagingIterableExtensions.all
import com.killrvideo.service.user.dao.UserCredentialsDao
import com.killrvideo.service.user.dao.UserDao
import com.killrvideo.service.user.dao.UserMapper
import com.killrvideo.service.user.dto.User
import com.killrvideo.service.user.dto.UserCredentials
import io.grpc.Status
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Handling user.
 *
 * @author DataStax Developer Advocates Team
 */
@Repository
open class UserRepository(mapper: UserMapper) {
    private val logger = KotlinLogging.logger {  }
    private val userDao: UserDao = mapper.userDao
    private val userCredentialsDao: UserCredentialsDao = mapper.userCredentialsDao

    /**
     * Create user Asynchronously composing things. (with Mappers)
     *
     * @param user           user Management
     * @param hashedPassword hashed Password
     * @return
     */
    suspend fun createUserAsync(user: User, hashedPassword: String): Boolean {
        val userCredentials: UserCredentials = UserCredentials.from(user, hashedPassword)
        return userCredentialsDao.insert(userCredentials)
            .thenApply { success ->
                duplicateCheck(userCredentials, !success)
                success
            }
            .thenCompose { _ -> userDao.insert(user) }
            .await()
    }

    private fun duplicateCheck(userCredentials: UserCredentials, duplicate: Boolean) {
        if (duplicate) {
            val errMsg = "Exception creating user because it already exists with email ${userCredentials.email}"
            logger.error(errMsg)
            throw Status.ALREADY_EXISTS.withDescription(errMsg).asRuntimeException()
        }
    }

    /**
     * Get user Credentials
     *
     * @param email
     * @return
     */
    suspend fun getUserCredentialAsync(email: String): UserCredentials? =
        userCredentialsDao.getUserCredential(email).await()

    /**
     * Retrieve user profiles.
     *
     * @param userids
     * @return
     */
    suspend fun getUserProfilesAsync(userids: List<UUID>): List<User> =
        userDao.getUserProfiles(userids)
            .thenApply { it.all() }.await()
}
