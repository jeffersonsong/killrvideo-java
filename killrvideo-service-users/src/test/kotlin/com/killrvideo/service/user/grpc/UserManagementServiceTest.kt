package com.killrvideo.service.user.grpc

import com.killrvideo.service.user.dto.User
import com.killrvideo.service.user.dto.UserCredentials
import com.killrvideo.service.user.repository.UserRepository
import com.killrvideo.utils.HashUtils
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.*

class UserManagementServiceTest {
    @InjectMockKs
    private lateinit var service: UserManagementService
    @MockK
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testCreateUserWithCreateFailure() {
        val user = mockk<User>();
        coEvery { userRepository.createUserAsync(any(), any()) } throws Status.INTERNAL.asRuntimeException()
        val result = runBlocking {
                service.createUser(user, "passwd")
            }
        assertTrue(result.isFailure)
    }

    @Test
    fun testCreateUser() {
        val user = mockk<User>();
        coEvery { userRepository.createUserAsync(any(), any()) } returns true
        val result = runBlocking {
            service.createUser(user, "passwd")
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun testVerifyCredentialsWithQueryFailure() {
        coEvery { userRepository.getUserCredentialAsync(any()) } throws Status.INTERNAL.asRuntimeException()
        val result = runBlocking { service.verifyCredentials("joe@gmail.com", "xxx") }
        assertTrue(result.isFailure)
    }

    @Test
    fun testVerifyCredentialsNotFound() {
        coEvery { userRepository.getUserCredentialAsync(any()) } returns null
        val result = runBlocking { service.verifyCredentials("joe@gmail.com", "xxx") }
        assertTrue(result.isFailure)
    }

    @Test
    fun testVerifyCredentialsWithPasswdMismatch() {
        val credentials = userCredentials("joe@gmail.com", "xxx", UUID.randomUUID())
        coEvery { userRepository.getUserCredentialAsync(any()) } returns credentials
        val result = runBlocking { service.verifyCredentials("joe@gmail.com", "passwd") }
        assertTrue(result.isFailure)
    }

    @Test
    fun testVerifyCredentials() {
        val userid = UUID.randomUUID()
        val passwd = "passwd"
        val credentials = userCredentials("joe@gmail.com", HashUtils.hashPassword(passwd), userid)
        coEvery { userRepository.getUserCredentialAsync(any()) } returns credentials
        val result = runBlocking { service.verifyCredentials("joe@gmail.com", passwd) }
        assertTrue(result.isSuccess)
    }

    @Test
    fun testGetUserProfileWithEmptyUserIds() {
        val result = runBlocking {
            service.getUserProfile(emptyList())
        }
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun testGetUserProfileWithQueryFailure() {
        coEvery {
            userRepository.getUserProfilesAsync(any())
        } throws Status.INTERNAL.asRuntimeException()

        val userId = UUID.randomUUID()
        val result = runBlocking { service.getUserProfile(listOf(userId)) }
        assertTrue(result.isFailure)
    }

    @Test
    fun testGetUserProfile() {
        val user = mockk<User>()
        val users = listOf(user)
        coEvery {
            userRepository.getUserProfilesAsync(any())
        } returns users

        val userId = UUID.randomUUID()
        val result = runBlocking { service.getUserProfile(listOf(userId)) }
        assertTrue(result.isSuccess)
        assertEquals(users, result.getOrNull()!!)
    }

    private fun userCredentials(email: String, passwd: String, userid: UUID): UserCredentials {
        val user = mockk<User>()
        every { user.email } returns email
        every { user.userid } returns userid
        return UserCredentials.from(user, passwd)
    }
}
