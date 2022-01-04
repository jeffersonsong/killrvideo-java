package com.killrvideo.service.user.repository

import com.killrvideo.exception.AlreadyExistsException
import com.killrvideo.service.user.dao.UserCredentialsDao
import com.killrvideo.service.user.dao.UserDao
import com.killrvideo.service.user.dao.UserMapper
import com.killrvideo.service.user.dto.User
import com.killrvideo.service.user.dto.UserCredentials
import com.killrvideo.utils.test.CassandraTestUtilsKt.mockMappedAsyncPagingIterable
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import java.util.concurrent.CompletableFuture

internal class UserRepositoryTest {
    private lateinit var repository: UserRepository
    private lateinit var userDao: UserDao
    private lateinit var userCredentialsDao: UserCredentialsDao

    @BeforeEach
    fun setUp() {
        userDao = mockk()
        userCredentialsDao = mockk()

        val mapper = mockk<UserMapper>()
        every { mapper.userDao } returns userDao
        every { mapper.userCredentialsDao } returns userCredentialsDao

        repository = UserRepository(mapper)
    }

    @Test
    fun testCreateUserAsyncWithUserAlreadyExists() {
        val user = User(userid = UUID.randomUUID(), email = "joe@gmail.com")
        every { userCredentialsDao.insert(any()) } returns CompletableFuture.completedFuture(false)

        assertThrows<AlreadyExistsException> {
            runBlocking {
                repository.createUserAsync(user, "passwd")
            }
        }
    }

    @Test
    fun testCreateUserAsync() {
        val user = User(userid = UUID.randomUUID(), email = "joe@gmail.com")
        every { userCredentialsDao.insert(any()) } returns CompletableFuture.completedFuture(true)
        every { userDao.insert(any()) } returns CompletableFuture.completedFuture(true)

        val result = runBlocking {
            repository.createUserAsync(user, "passwd")
        }
        assertTrue(result)
    }

    @Test
    fun testGetUserCredentialAsync() {
        val credentials = mockk<UserCredentials>()
        every { userCredentialsDao.getUserCredential(any()) } returns CompletableFuture.completedFuture(credentials)
        val result = runBlocking {
            repository.getUserCredentialAsync("joe@gmail.com")
        }
        assertEquals(credentials, result)
    }

    @Test
    fun testGetUserProfilesAsync() {
        val userid = UUID.randomUUID()
        val user = mockk<User>()
        val userids = listOf(userid)
        val users = listOf(user)
        val iter = mockMappedAsyncPagingIterable(users)
        every { userDao.getUserProfiles(any()) } returns CompletableFuture.completedFuture(iter)

        val result = runBlocking {
            repository.getUserProfilesAsync(userids)
        }
        assertEquals(1, result.size)
    }
}
