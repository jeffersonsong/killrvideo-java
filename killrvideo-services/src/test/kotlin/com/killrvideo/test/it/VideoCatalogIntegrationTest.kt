package com.killrvideo.test.it

import com.killrvideo.KillrvideoServicesGrpcClient
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest

object VideoCatalogIntegrationTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val client = KillrvideoServicesGrpcClient("localhost", 8899)
        val creRequest = VerifyCredentialsRequest.newBuilder()
            .setEmail("a.a@a.com")
            .setPassword("aaa")
            .build()
        val res = client.userServiceGrpcClient!!.verifyCredentials(creRequest)
        println(res.userId)
    }
}
