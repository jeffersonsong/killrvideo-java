package com.killrvideo.service.user.grpc;

import com.google.protobuf.Timestamp;
import com.killrvideo.service.user.dto.User;
import com.killrvideo.utils.GrpcMappingUtils;
import killrvideo.user_management.UserManagementServiceOuterClass.*;
import killrvideo.user_management.events.UserManagementEvents;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.fromUuid;
import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
        user.setUserid(fromUuid(grpcReq.getUserId()));
        return user;
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
                .filter(uuid -> isNotBlank(uuid.getValue()))
                .map(GrpcMappingUtils::fromUuid)
                .toArray(UUID[]::new));
    }

    public GetUserProfileResponse buildGetUserProfileResponse(List<User> users) {
        final GetUserProfileResponse.Builder builder = GetUserProfileResponse.newBuilder();
        users.stream()
                .map(this::mapUserToGrpcUserProfile)
                .forEach(builder::addProfiles);
        return builder.build();
    }

    private UserProfile mapUserToGrpcUserProfile(User user) {
        return UserProfile.newBuilder()
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstname())
                .setLastName(user.getLastname())
                .setUserId(uuidToUuid(user.getUserid()))
                .build();
    }
}
