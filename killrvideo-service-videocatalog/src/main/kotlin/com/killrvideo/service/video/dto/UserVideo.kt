package com.killrvideo.service.video.dto

import com.datastax.oss.driver.api.mapper.annotations.*
import java.time.Instant
import java.util.*

/**
 * Pojo representing DTO for table 'user_videos'
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("user_videos")
data class UserVideo(
    @PartitionKey var userid: UUID? = null,
    @ClusteringColumn(0) var addedDate: Instant? = null,
    @ClusteringColumn(1) var videoid: UUID? = null,
    var name: String? = null,
    var previewImageLocation: String? = null
) {
    companion object {
        fun from(v: Video, now: Instant): UserVideo =
            UserVideo(
                userid = v.userid,
                addedDate = now,
                videoid = v.videoid,
                name = v.name,
                previewImageLocation = v.previewImageLocation
            )
    }
}
