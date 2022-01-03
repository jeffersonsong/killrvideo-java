package com.killrvideo.service.search.dao

import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.service.search.dto.Video
import org.springframework.stereotype.Component

@Component("searchVideoRowMapper")
class VideoRowMapper {
    fun map(row: Row): Video {
        return Video(
            row.getUuid(Video.COLUMN_VIDEOID),
            row.getUuid(Video.COLUMN_USERID),
            row.getString(Video.COLUMN_NAME),
            row.getString(Video.COLUMN_DESCRIPTION),
            row.getString(Video.COLUMN_LOCATION),
            row.getInt(Video.COLUMN_LOCATIONTYPE),
            row.getString(Video.COLUMN_PREVIEW),
            row.getSet(Video.COLUMN_TAGS, String::class.java),
            row.getInstant(Video.COLUMN_ADDED_DATE)
        )
    }
}