package com.killrvideo.service.rating.dto

import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import java.util.*

/**
 * Pojo representing DTO for table 'video_ratings'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("video_ratings")
data class VideoRating(
    @PartitionKey var videoid: UUID?,
    var ratingCounter: Long = 0L,
    var ratingTotal: Long = 0
)
