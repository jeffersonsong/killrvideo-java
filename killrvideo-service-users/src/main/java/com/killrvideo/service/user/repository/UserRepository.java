package com.killrvideo.service.user.repository;

import com.killrvideo.dse.utils.MappedAsyncPagingIterableUtils;
import com.killrvideo.service.user.dao.UserCredentialsDao;
import com.killrvideo.service.user.dao.UserDao;
import com.killrvideo.service.user.dao.UserMapper;
import com.killrvideo.service.user.dto.User;
import com.killrvideo.service.user.dto.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Handling user.
 *
 * @author DataStax Developer Advocates Team
 */
@Repository
public class UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);
    private final UserDao userDao;
    private final UserCredentialsDao userCredentialsDao;

    public UserRepository(UserMapper mapper) {
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
    public CompletableFuture<Boolean> createUserAsync(User user, String hashedPassword) {
        UserCredentials userCredentials = UserCredentials.from(user, hashedPassword);

        return userCredentialsDao.insert(userCredentials)
                .thenApply(rs -> validationInsertion(userCredentials, rs))
                .thenCompose(rs -> userDao.insert(user));
    }

    private boolean validationInsertion(UserCredentials userCredentials, boolean insertionSuccess) {
        if (!insertionSuccess) {
            String errMsg = String.format("Exception creating user because it already exists with email %s",
                    userCredentials.getEmail());
            LOGGER.error(errMsg);
            throw new CompletionException(errMsg, new IllegalArgumentException(errMsg));
        } else {
            return true;
        }
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
