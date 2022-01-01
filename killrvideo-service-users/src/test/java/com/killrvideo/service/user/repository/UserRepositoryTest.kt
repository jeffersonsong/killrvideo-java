package com.killrvideo.service.user.repository;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.killrvideo.service.user.dao.UserCredentialsDao;
import com.killrvideo.service.user.dao.UserDao;
import com.killrvideo.service.user.dao.UserMapper;
import com.killrvideo.service.user.dto.User;
import com.killrvideo.service.user.dto.UserCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.killrvideo.utils.test.CassandraTestUtils.mockMappedAsyncPagingIterable;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {
    private UserRepository repository;
    private UserDao userDao;
    private UserCredentialsDao userCredentialsDao;

    @BeforeEach
    public void setUp() {
        this.userDao = mock(UserDao.class);
        this.userCredentialsDao = mock(UserCredentialsDao.class);
        UserMapper mapper = mock(UserMapper.class);
        when(mapper.getUserDao()).thenReturn(userDao);
        when(mapper.getUserCredentialsDao()).thenReturn(userCredentialsDao);

        this.repository = new UserRepository(mapper);
    }

    @Test
    public void testCreateUserAsyncWithUserAlreadyExists() {
        User user = user(UUID.randomUUID(), "joe@gmail.com", "passwd");
        when(userCredentialsDao.insert(any())).thenReturn(
                CompletableFuture.completedFuture(false)
        );

        this.repository.createUserAsync(user, "passwd").whenComplete((result, error) -> {
            assertNull(result);
            assertNotNull(error);
        });
        verify(this.userDao, times(0)).insert(any());
    }

    @Test
    public void testCreateUserAsync() {
        User user = user(UUID.randomUUID(), "joe@gmail.com", "passwd");
        when(userCredentialsDao.insert(any())).thenReturn(
                CompletableFuture.completedFuture(true)
        );
        when(this.userDao.insert(any())).thenReturn(CompletableFuture.completedFuture(true));

        this.repository.createUserAsync(user, "passwd").whenComplete((result, error) -> {
            assertTrue(result);
            assertNull(error);
        });
    }

    @Test
    public void testGetUserCredentialAsync() {
        UserCredentials credentials = mock(UserCredentials.class);
        when(this.userCredentialsDao.getUserCredential(any())).thenReturn(
                CompletableFuture.completedFuture(credentials)
        );
        this.repository.getUserCredentialAsync("joe@gmail.com").whenComplete((result, error) -> {
            assertEquals(credentials, result);
            assertNull(error);
        });
    }

    @Test
    public void testGetUserProfilesAsync() {
        UUID userid = UUID.randomUUID();
        User user = mock(User.class);
        List<UUID> userids = singletonList(userid);
        List<User> users = singletonList(user);

        MappedAsyncPagingIterable<User> iter = mockMappedAsyncPagingIterable(users);
        when(this.userDao.getUserProfiles(any())).thenReturn(
                CompletableFuture.completedFuture(iter)
        );
        this.repository.getUserProfilesAsync(userids).whenComplete((result, error) -> {
            assertEquals(1, result.size());
            assertNull(error);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private User user(UUID userid, String email, String passwd) {
        User user = new User();
        user.setUserid(userid);
        user.setEmail(email);
        return user;
    }
}
