package com.killrvideo.service.search.dao;

import com.datastax.oss.driver.api.core.cql.Row;
import com.killrvideo.service.search.dto.Video;
import org.springframework.stereotype.Component;

import static com.killrvideo.service.search.dto.Video.*;

@Component("searchVideoRowMapper")
public class VideoRowMapper {
    public Video map(Row row) {
        return new Video(
                row.getUuid(COLUMN_VIDEOID),
                row.getUuid(COLUMN_USERID),
                row.getString(COLUMN_NAME),
                row.getString(COLUMN_DESCRIPTION),
                row.getString(COLUMN_LOCATION),
                row.getInt(COLUMN_LOCATIONTYPE),
                row.getString(COLUMN_PREVIEW),
                row.getSet(COLUMN_TAGS, String.class),
                row.getInstant(COLUMN_ADDED_DATE)
        );
    }
}
