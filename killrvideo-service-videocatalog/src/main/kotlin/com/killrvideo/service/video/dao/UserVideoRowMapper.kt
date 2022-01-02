package com.killrvideo.service.video.dao

import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import org.springframework.stereotype.Component

@Component
class UserVideoRowMapper {
    fun map(row: Row): UserVideo =
        UserVideo(
            userid = row.getUuid(Video.COLUMN_USERID),
            addedDate = row.getInstant(Video.COLUMN_ADDED_DATE),
            videoid = row.getUuid(Video.COLUMN_VIDEOID),
            name = row.getString(Video.COLUMN_NAME),
            previewImageLocation = row.getString(Video.COLUMN_PREVIEW)
        )
}
