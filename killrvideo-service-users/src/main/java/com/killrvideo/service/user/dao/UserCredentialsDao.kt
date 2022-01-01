package com.killrvideo.service.user.dao;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes;
import com.killrvideo.service.user.dto.UserCredentials;

import java.util.concurrent.CompletableFuture;

@Dao
public interface UserCredentialsDao {
    @Insert(ifNotExists = true)
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    CompletableFuture<Boolean> insert(UserCredentials userCredentials);

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<UserCredentials> getUserCredential(String email);
}
