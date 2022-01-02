package com.killrvideo.service.statistic.dto

import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import java.util.*

/**
 * Pojo representing DTO for table 'video_playback_stats'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("video_playback_stats")
data class VideoPlaybackStats(
    @PartitionKey
    var videoid: UUID?,

    /**
     * "views" column is a counter type in the underlying DSE database.  As of driver version 3.2 there
     * is no "@Counter" annotation that I know of.  No worries though, just use the incr() function
     * while using the QueryBuilder.  Something similar to with(QueryBuilder.incr("views")).
     */
    var views: Long?
)
