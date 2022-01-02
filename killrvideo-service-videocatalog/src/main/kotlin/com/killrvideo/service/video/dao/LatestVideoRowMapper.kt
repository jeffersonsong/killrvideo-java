package com.killrvideo.service.video.dao

import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.service.video.dto.LatestVideo
import com.killrvideo.service.video.dto.Video
import org.springframework.stereotype.Component

@Component
class LatestVideoRowMapper {
    fun map(row: Row): LatestVideo =
        LatestVideo(
            yyyymmdd = row.getString(LatestVideo.COLUMN_YYYYMMDD),
            addedDate = row.getInstant(Video.COLUMN_ADDED_DATE),
            videoid = row.getUuid(Video.COLUMN_VIDEOID),
            userid = row.getUuid(Video.COLUMN_USERID),
            name = row.getString(Video.COLUMN_NAME),
            previewImageLocation = row.getString(Video.COLUMN_PREVIEW)
        )
}
