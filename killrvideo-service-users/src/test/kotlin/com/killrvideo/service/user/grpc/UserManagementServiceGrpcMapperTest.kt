package com.killrvideo.service.user.grpc

import com.killrvideo.service.user.dto.User
import com.killrvideo.service.user.grpc.UserManagementServiceGrpcMapper.CreateUserRequestExtensions.parse
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.user_management.createUserRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class UserManagementServiceGrpcMapperTest {
    private val mapper = UserManagementServiceGrpcMapper()
    @Test
    fun testMapUserRequest2User() {
        val u: User = user()
        val request = createUserRequest {
            email = u.email!!
            firstName = u.firstname!!
            lastName = u.lastname!!
            userId = uuidToUuid(u.userid!!)
        }
        val (user, _) = request.parse()
        assertEquals(u.email, user.email)
        assertEquals(u.firstname, user.firstname)
        assertEquals(u.lastname, user.lastname)
        assertEquals(u.userid, user.userid)
    }

    @Test
    fun testMapResponseVerifyCredentials() {
        val userid = UUID.randomUUID()
        val response = mapper.mapResponseVerifyCredentials(userid)
        assertEquals(userid.toString(), response.userId.value)
    }

    @Test
    fun testCreateUserCreatedEvent() {
        val u = user()
        val event = mapper.createUserCreatedEvent(u)
        assertEquals(u.email, event.email)
        assertEquals(u.firstname, event.firstName)
        assertEquals(u.lastname, event.lastName)
        assertEquals(u.userid.toString(), event.userId.value)
    }

    @Test
    fun testBuildGetUserProfileResponse() {
        val u = user()
        val response = mapper.buildGetUserProfileResponse(listOf(u))
        assertEquals(1, response.profilesCount)
        val profile = response.getProfiles(0)
        assertEquals(u.email, profile.email)
        assertEquals(u.firstname, profile.firstName)
        assertEquals(u.lastname, profile.lastName)
        assertEquals(u.userid.toString(), profile.userId.value)
    }

    private fun user(): User {
        val userid = UUID.randomUUID()
        val email = "joe@gmail.com"
        val firstName = "first name"
        val lastName = "last name"
        return User(
            userid = userid,
            email = email,
            firstname = firstName,
            lastname = lastName,
            createdDate = Instant.now()
        )
    }
}
