package com.killrvideo.service.user.grpc;

import com.killrvideo.service.user.dto.User;
import com.killrvideo.utils.GrpcMappingUtils;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.UserProfile;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapping from interfaces GRPC to DTO
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class UserManagementServiceGrpcMapper {
    
    /** Hide constructor for utility class. */
    private UserManagementServiceGrpcMapper() {
    }
    
    public static User mapUserRequest2User(CreateUserRequest grpcReq) {
        User user = new User();
        user.setEmail(grpcReq.getEmail());
        user.setCreatedDate(Instant.now());
        user.setFirstname(grpcReq.getFirstName());
        user.setLastname(grpcReq.getLastName());
        user.setUserid(UUID.fromString(grpcReq.getUserId().getValue()));
        return user;
    }
    
    public static UserProfile mapUserToGrpcUserProfile(User user) {
       return UserProfile.newBuilder()
                    .setEmail(user.getEmail())
                    .setFirstName(user.getFirstname())
                    .setLastName(user.getLastname())
                    .setUserId(GrpcMappingUtils.uuidToUuid(user.getUserid()))
                    .build();
    }
    
    public static VerifyCredentialsResponse mapResponseVerifyCredentials(UUID userid) {
        return VerifyCredentialsResponse.newBuilder().setUserId(GrpcMappingUtils.uuidToUuid(userid)).build();
    }

}
