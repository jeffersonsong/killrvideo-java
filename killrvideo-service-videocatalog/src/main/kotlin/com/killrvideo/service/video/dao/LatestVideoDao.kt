package com.killrvideo.service.video.dao

import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Insert
import com.datastax.oss.driver.api.mapper.annotations.StatementAttributes
import com.killrvideo.service.video.dto.LatestVideo
import java.util.concurrent.CompletableFuture

@Dao
interface LatestVideoDao {
    @Insert
    @StatementAttributes(consistencyLevel = "LOCAL_QUORUM")
    fun insert(latestVideo: LatestVideo): CompletableFuture<Void>
}
