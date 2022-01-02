package com.killrvideo.service.rating.dto

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn
import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import java.util.*

/**
 * Pojo representing DTO for table 'video_ratings_by_user'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("video_ratings_by_user")
data class VideoRatingByUser(
    @PartitionKey
    var videoid: UUID?,
    @ClusteringColumn
    var userid: UUID?,
    var rating: Int = 0
)
