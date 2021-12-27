package com.killrvideo.service.user.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.killrvideo.service.user.dao.UserCredentialsDao;
import com.killrvideo.service.user.dao.UserDao;
import com.killrvideo.service.user.dao.UserMapper;
import com.killrvideo.service.user.dto.User;
import com.killrvideo.service.user.dto.UserCredentials;
import com.killrvideo.dse.utils.MappedAsyncPagingIterableUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handling user.
 *
 * @author DataStax Developer Advocates Team
 */
@Repository
public class UserRepository {
    private UserDao userDao;
    private UserCredentialsDao userCredentialsDao;

    public UserRepository(CqlSession session) {
        UserMapper mapper = UserMapper.builder(session).build();
        this.userDao = mapper.getUserDao();
        this.userCredentialsDao = mapper.getUserCredentialsDao();
    }

    /**
     * Create user Asynchronously composing things. (with Mappers)
     *
     * @param user           user Management
     * @param hashedPassword hashed Password
     * @return
     */
    public CompletableFuture<User> createUserAsync(User user, String hashedPassword) {
        return userCredentialsDao.insert(UserCredentials.from(user, hashedPassword))
                .thenCompose(rs -> userDao.insert(user));
    }

    /**
     * Get user Credentials
     *
     * @param email
     * @return
     */
    public CompletableFuture<UserCredentials> getUserCredentialAsync(String email) {
        return userCredentialsDao.getUserCredential(email);
    }

    /**
     * Retrieve user profiles.
     *
     * @param userids
     * @return
     */
    public CompletableFuture<List<User>> getUserProfilesAsync(List<UUID> userids) {
        return userDao.getUserProfiles(userids)
                .thenApply(MappedAsyncPagingIterableUtils::all);
    }
}
