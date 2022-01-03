package com.killrvideo.test.it;

import com.killrvideo.KillrvideoServicesGrpcClient;

import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse;

public class VideoCatalogIntegrationTest {
    
    public static void main(String[] args) {
        KillrvideoServicesGrpcClient client = 
                new KillrvideoServicesGrpcClient("localhost", 8899);

        VerifyCredentialsRequest creRequest = VerifyCredentialsRequest.newBuilder()
                .setEmail("a.a@a.com")
                .setPassword("aaa")
                .build();
               
        VerifyCredentialsResponse res = client.userServiceGrpcClient.verifyCredentials(creRequest);
        System.out.println(res.getUserId());
        
        
    }

}
