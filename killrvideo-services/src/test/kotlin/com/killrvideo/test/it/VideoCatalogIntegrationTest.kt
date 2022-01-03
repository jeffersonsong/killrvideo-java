package com.killrvideo.test.it

import com.killrvideo.KillrvideoServicesGrpcClient
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest
import killrvideo.user_management.verifyCredentialsRequest

object VideoCatalogIntegrationTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val client = KillrvideoServicesGrpcClient("localhost", 8899)
        val creRequest = verifyCredentialsRequest {
            email = "a.a@a.com"
            password = "aaa"
        }
        val res = client.userServiceGrpcClient.verifyCredentials(creRequest)
        println(res.userId)
    }
}
