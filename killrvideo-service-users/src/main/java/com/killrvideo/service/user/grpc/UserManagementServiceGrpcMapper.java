package com.killrvideo.service.user.grpc;

import com.google.protobuf.Timestamp;
import com.killrvideo.service.user.dto.User;
import killrvideo.user_management.UserManagementServiceOuterClass.*;
import killrvideo.user_management.events.UserManagementEvents;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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

    public List<UUID> mapToListOfUserid(GetUserProfileRequest grpcReq) {
        return Arrays.asList(grpcReq
                .getUserIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .toArray(UUID[]::new));
    }

    public GetUserProfileResponse buildGetUserProfileResponse(List<User> users) {
        final GetUserProfileResponse.Builder builder = GetUserProfileResponse.newBuilder();
        users.stream()
                .map(this::mapUserToGrpcUserProfile)
                .forEach(builder::addProfiles);
        return builder.build();
    }
}
