package com.killrvideo.service.user.dao

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import com.datastax.oss.driver.api.mapper.annotations.*
import com.killrvideo.service.user.dto.User
import java.util.*
import java.util.concurrent.CompletableFuture

@Dao
interface UserDao {
    @Insert(ifNotExists = true)
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun insert(user: User): CompletableFuture<Boolean>

    @Select(customWhereClause = "userid in :userids")
    @StatementAttributes(consistencyLevel = "LOCAL_ONE")
    fun getUserProfiles(@CqlName("userids") userids: List<UUID>):
            CompletableFuture<MappedAsyncPagingIterable<User>>
}
