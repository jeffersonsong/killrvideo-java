package com.killrvideo.service.user.grpc;

import com.killrvideo.service.user.dto.User;
import killrvideo.user_management.UserManagementServiceOuterClass.*;
import killrvideo.user_management.events.UserManagementEvents;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.killrvideo.utils.GrpcMappingUtils.uuidToUuid;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class UserManagementServiceGrpcMapperTest {
    private UserManagementServiceGrpcMapper mapper = new UserManagementServiceGrpcMapper();

    @Test
    public void testMapUserRequest2User() {
        User u = user();
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail(u.getEmail())
                .setFirstName(u.getFirstname())
                .setLastName(u.getLastname())
                .setUserId(uuidToUuid(u.getUserid()))
                .build();

        User user = mapper.mapUserRequest2User(request);
        assertEquals(u.getEmail(), user.getEmail());
        assertEquals(u.getFirstname(), user.getFirstname());
        assertEquals(u.getLastname(), user.getLastname());
        assertEquals(u.getUserid(), user.getUserid());
    }

    @Test
    public void testMapResponseVerifyCredentials() {
        UUID userid = UUID.randomUUID();
        VerifyCredentialsResponse response = mapper.mapResponseVerifyCredentials(userid);
        assertEquals(userid.toString(), response.getUserId().getValue());
    }

    @Test
    public void testCreateUserCreatedEvent() {
        User u = user();
        UserManagementEvents.UserCreated event = mapper.createUserCreatedEvent(u);
        assertEquals(u.getEmail(), event.getEmail());
        assertEquals(u.getFirstname(), event.getFirstName());
        assertEquals(u.getLastname(), event.getLastName());
        assertEquals(u.getUserid().toString(), event.getUserId().getValue());
    }

    @Test
    public void testMapToListOfUserid() {
        UUID userid1 = UUID.randomUUID();
        UUID userid2 = UUID.randomUUID();
        GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
                .addUserIds(uuidToUuid(userid1))
                .addUserIds(uuidToUuid(userid2))
                .build();

        List<UUID> userids = mapper.mapToListOfUserid(request);
        assertEquals(2, userids.size());
    }

    @Test
    public void testBuildGetUserProfileResponse() {
        User u = user();
        GetUserProfileResponse response = mapper.buildGetUserProfileResponse(singletonList(u));
        assertEquals(1, response.getProfilesCount());

        UserProfile profile = response.getProfiles(0);

        assertEquals(u.getEmail(), profile.getEmail());
        assertEquals(u.getFirstname(), profile.getFirstName());
        assertEquals(u.getLastname(), profile.getLastName());
        assertEquals(u.getUserid().toString(), profile.getUserId().getValue());
    }

    private User user() {
        UUID userid = UUID.randomUUID();
        String email = "joe@gmail.com";
        String firstName = "first name";
        String lastName = "last name";
        User user = new User();
        user.setUserid(userid);
        user.setEmail(email);
        user.setFirstname(firstName);
        user.setLastname(lastName);
        user.setCreatedDate(Instant.now());
        return user;
    }
}