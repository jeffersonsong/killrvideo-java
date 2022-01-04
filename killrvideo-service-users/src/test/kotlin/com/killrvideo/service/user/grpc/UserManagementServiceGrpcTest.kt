package com.killrvideo.service.user.grpc

import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.user.dto.User
import com.killrvideo.service.user.dto.UserCredentials
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import com.killrvideo.utils.HashUtils
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import killrvideo.user_management.*
import killrvideo.user_management.UserManagementServiceOuterClass.UserProfile
import killrvideo.user_management.events.UserManagementEvents.UserCreated
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import java.util.concurrent.CompletableFuture

internal class UserManagementServiceGrpcTest {
    @InjectMockKs
    private lateinit var service: UserManagementServiceGrpc

    @MockK
    private lateinit var userService: UserManagementService

    @MockK
    private lateinit var messagingDao: MessagingDao

    @MockK
    private lateinit var validator: UserManagementServiceGrpcValidator

    @MockK
    private lateinit var mapper: UserManagementServiceGrpcMapper

    private val topicUserCreated = "topic-kv-userCreation"
    val serviceKey = "UserManagementService"

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun testCreateUserWithValidationFailure() {
        val request = createUserRequest {}

        every {
            validator.validateGrpcRequest_createUser(any())
        } throws IllegalArgumentException()

        assertThrows<IllegalArgumentException> {
            runBlocking {
                service.createUser(request)
            }
        }
    }

    @Test
    fun testCreateUserWithCreateFailure() {
        val userid = UUID.randomUUID()
        val request = createUserRequest { password = "passwd"; email = "joe@gmail.com"; userId=uuidToUuid(userid) }
        every { validator.validateGrpcRequest_createUser(any()) } just Runs

        coEvery { userService.createUser(any(), any()) } returns Result.failure(Exception())

        assertThrows<Exception> {
            runBlocking {
                service.createUser(request)
            }
        }
    }

    @Test
    fun testCreateUser() {
        val userid = UUID.randomUUID()
        val request = createUserRequest { password = "passwd"; email = "joe@gmail.com"; userId=uuidToUuid(userid) }
        val user = mockk<User>()
        every { validator.validateGrpcRequest_createUser(any()) } just Runs

        val event = UserCreated.getDefaultInstance()
        every { mapper.createUserCreatedEvent(any()) } returns event

        coEvery { userService.createUser(any(), any()) } returns Result.success(true)
        every { messagingDao.sendEvent(any(), any()) } returns CompletableFuture.completedFuture(null)

        runBlocking {
            service.createUser(request)
        }

        coVerify {
            userService.createUser(any(), any())
        }

        verify(exactly = 1) {
            messagingDao.sendEvent(any(), any())
        }
    }

    @Test
    fun testVerifyCredentialsWithValidationFailure() {
        val request = verifyCredentialsRequest {}
        every { validator.validateGrpcRequest_VerifyCredentials(any()) } throws IllegalArgumentException()

        assertThrows<IllegalArgumentException> {
            runBlocking {
                service.verifyCredentials(request)
            }
        }
    }

    @Test
    fun testVerifyCredentialsWithQueryFailure() {
        val request = verifyCredentialsRequest { email = "joe@gmail.com"; password = "passwd" }
        every { validator.validateGrpcRequest_VerifyCredentials(any()) } just Runs
        coEvery { userService.verifyCredentials(any(), any()) } returns Result.failure(Exception())

        assertThrows<Exception> {
            runBlocking {
                service.verifyCredentials(request)
            }
        }
    }

    @Test
    fun testVerifyCredentials() {
        val request = verifyCredentialsRequest { email = "joe@gmail.com"; password = "passwd" }
        every { validator.validateGrpcRequest_VerifyCredentials(any()) } just Runs

        val userid = UUID.randomUUID()
        val credentials = userCredentials("joe@gmail.com", HashUtils.hashPassword("passwd"), userid)
        coEvery { userService.verifyCredentials(any(), any()) } returns Result.success(credentials)
        val response = verifyCredentialsResponse {
            userId = uuidToUuid(userid)
        }
        every { mapper.mapResponseVerifyCredentials(any()) } returns response

        val result = runBlocking {
            service.verifyCredentials(request)
        }

        assertEquals(userid.toString(), result.userId.value)
    }

    @Test
    fun testGetUserProfileWithValidationFailure() {
        val request = getUserProfileRequest {}
        every { validator.validateGrpcRequest_getUserProfile(any()) } throws IllegalArgumentException()
        assertThrows<IllegalArgumentException> {
            runBlocking {
                service.getUserProfile(request)
            }
        }
    }

    @Test
    fun testGetUserProfileWithQueryFailure() {
        val userId = UUID.randomUUID()
        val request = getUserProfileRequest {
            userIds.add(uuidToUuid(userId))
        }
        every { validator.validateGrpcRequest_getUserProfile(any()) } just Runs
        coEvery {
            userService.getUserProfile(any())
        } returns Result.failure(Exception())

        assertThrows<Exception> {
            runBlocking {
                service.getUserProfile(request)
            }
        }
    }

    @Test
    fun testGetUserProfile() {
        val userid = UUID.randomUUID()
        val request = getUserProfileRequest {
            userIds.add(uuidToUuid(userid))
        }

        every { validator.validateGrpcRequest_getUserProfile(any()) } just Runs
        val user = mockk<User>()
        val users = listOf(user)
        val profile = mockk<UserProfile>()
        val response = getUserProfileResponse {
            profiles.add(profile)
        }
        every { mapper.buildGetUserProfileResponse(users) } returns response
        coEvery {
            userService.getUserProfile(any())
        } returns Result.success(users)
        val result = runBlocking {
            service.getUserProfile(request)
        }
        assertEquals(1, result.profilesCount)
    }

    private fun userCredentials(email: String, passwd: String, userid: UUID): UserCredentials {
        val user = mockk<User>()
        every { user.email } returns email
        every { user.userid } returns userid
        return UserCredentials.from(user, passwd)
    }
}
