package com.killrvideo.service.user.grpc

import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import killrvideo.user_management.createUserRequest
import killrvideo.user_management.getUserProfileRequest
import killrvideo.user_management.verifyCredentialsRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.stream.Collectors
import java.util.stream.IntStream

internal class UserManagementServiceGrpcValidatorTest {
    private val validator = UserManagementServiceGrpcValidator()

    @Test
    fun testValidateGrpcRequest_createUser_Success() {
        val request = createUserRequest {
            userId = randomUuid()
            password = "passwd"
            email = "joe@gmail.com"
        }
        validator.validateGrpcRequest_createUser(request)
    }

    @Test
    fun testValidateGrpcRequest_createUser_Failure() {
        val request = createUserRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_createUser(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_VerifyCredentials_Success() {
        val request = verifyCredentialsRequest {
            password = "passwd"
            email = "joe@gmail.com"
        }
        validator.validateGrpcRequest_VerifyCredentials(request)
    }

    @Test
    fun testValidateGrpcRequest_VerifyCredentials_Failure() {
        val request = verifyCredentialsRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_VerifyCredentials(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_getUserProfile_Success() {
        val request = getUserProfileRequest {
            userIds.add(randomUuid())
        }
        validator.validateGrpcRequest_getUserProfile(request)
    }

    @Test
    fun testValidateGrpcRequest_getUserProfile_Failure() {
        val request = getUserProfileRequest {
            userIds.addAll(
                IntStream.range(0, 21).mapToObj { _ -> randomUuid() }.collect(Collectors.toList())
            )
        }
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_getUserProfile(request)
        }
    }
}
