package com.killrvideo.service.user.grpc;

import com.google.protobuf.Timestamp;
import com.killrvideo.service.user.dto.User;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.UserProfile;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse;
import killrvideo.user_management.events.UserManagementEvents;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;

/**
 * Mapping from interfaces GRPC to DTO
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
public class UserManagementServiceGrpcMapper {

    public User mapUserRequest2User(CreateUserRequest grpcReq) {
        User user = new User();
        user.setEmail(grpcReq.getEmail());
        user.setCreatedDate(Instant.now());
        user.setFirstname(grpcReq.getFirstName());
        user.setLastname(grpcReq.getLastName());
        user.setUserid(UUID.fromString(grpcReq.getUserId().getValue()));
        return user;
    }
    
    public UserProfile mapUserToGrpcUserProfile(User user) {
       return UserProfile.newBuilder()
                    .setEmail(user.getEmail())
                    .setFirstName(user.getFirstname())
                    .setLastName(user.getLastname())
                    .setUserId(uuidToUuid(user.getUserid()))
                    .build();
    }
    
    public VerifyCredentialsResponse mapResponseVerifyCredentials(UUID userid) {
        return VerifyCredentialsResponse.newBuilder().setUserId(uuidToUuid(userid)).build();
    }

    public UserManagementEvents.UserCreated createUserCreatedEvent(User user) {
        return UserManagementEvents.UserCreated.newBuilder()
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstname())
                .setLastName(user.getLastname())
                .setUserId(uuidToUuid(user.getUserid()))
                .setTimestamp(Timestamp.newBuilder().build())
                .build();
    }
}
