package com.killrvideo.service.user.dao

import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Insert
import com.datastax.oss.driver.api.mapper.annotations.Select
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes
import com.killrvideo.service.user.dto.UserCredentials
import java.util.concurrent.CompletableFuture

@Dao
interface UserCredentialsDao {
    @Insert(ifNotExists = true)
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun insert(userCredentials: UserCredentials): CompletableFuture<Boolean>

    @Select
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun getUserCredential(email: String): CompletableFuture<UserCredentials?>
}
