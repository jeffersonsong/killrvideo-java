package com.killrvideo.service.user.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.killrvideo.service.user.dao.UserCredentialsDao;
import com.killrvideo.service.user.dao.UserDao;
import com.killrvideo.service.user.dao.UserMapper;
import com.killrvideo.service.user.dto.User;
import com.killrvideo.service.user.dto.UserCredentials;
import com.killrvideo.utils.MappedAsyncPagingIterableUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public class UserRepository {
    private UserDao userDao;
    private UserCredentialsDao userCredentialsDao;

    public UserRepository(CqlSession session) {
        UserMapper mapper = UserMapper.builder(session).build();
        this.userDao = mapper.getUserDao();
        this.userCredentialsDao = mapper.getUserCredentialsDao();
    }

    public CompletableFuture<Void> createUserAsync(User user, String hashedPassword) {
        UserCredentials userCredentials = new UserCredentials(
                user.getEmail(), hashedPassword, user.getUserid()
        );
        CompletableFuture<Void> future1 = userCredentialsDao.insert(userCredentials);
        CompletableFuture<Void> future2 = userDao.insert(user);

        return CompletableFuture.allOf(future1, future2);
    }

    public CompletableFuture<UserCredentials> getUserCredentialAsync(String email) {
        return userCredentialsDao.getUserCredential(email);
    }

    public CompletableFuture<List<User>> getUserProfilesAsync(List<UUID> userids) {
        CompletableFuture<MappedAsyncPagingIterable<User>> future = userDao.getUserProfiles(userids);
        return future.thenApply(MappedAsyncPagingIterableUtils::toList);
    }
}
