package com.killrvideo.service.video.dto

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn
import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Pojo representing DTO for table 'latest_videos'
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("latest_videos")
data class LatestVideo(
    @PartitionKey val yyyymmdd: String?,
    @ClusteringColumn(0)
    var addedDate: Instant?,
    @ClusteringColumn(1)
    var videoid: UUID?,
    var userid: UUID?,
    var name: String?,
    var previewImageLocation: String?
) {
    companion object {
        private val DATEFORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC))

        const val COLUMN_YYYYMMDD = "yyyymmdd"
        fun from(v: Video, now: Instant): LatestVideo =
            LatestVideo(
                yyyymmdd = DATEFORMATTER.format(now),
                addedDate = now,
                videoid= v.videoid,
                userid = v.userid,
                name = v.name,
                previewImageLocation = v.previewImageLocation
            )
    }
}
