package com.killrvideo.service.video.dao;

import com.datastax.oss.driver.api.core.cql.Row;
import com.killrvideo.service.video.dto.LatestVideo;
import org.springframework.stereotype.Component;

import static com.killrvideo.dse.dto.Video.*;
import static com.killrvideo.service.video.dto.LatestVideo.COLUMN_YYYYMMDD;

@Component
public class LatestVideoRowMapper {
    public LatestVideo map(Row row) {
        return new LatestVideo(
                row.getString(COLUMN_YYYYMMDD),
                row.getUuid(COLUMN_USERID),
                row.getUuid(COLUMN_VIDEOID),
                row.getString(COLUMN_NAME),
                row.getString(COLUMN_PREVIEW),
                row.getInstant(COLUMN_ADDED_DATE)
        );
    }
}
