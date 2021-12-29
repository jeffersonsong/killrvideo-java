package com.killrvideo.service.user.dao;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;
import com.killrvideo.service.user.dto.User;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Dao
public interface UserDao {
    @Insert(ifNotExists = true)
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    CompletableFuture<Boolean> insert(User user);

    @Select(customWhereClause = "userid in :userids")
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    CompletableFuture<MappedAsyncPagingIterable<User>> getUserProfiles(@CqlName("userids") List<UUID> userids);
}
