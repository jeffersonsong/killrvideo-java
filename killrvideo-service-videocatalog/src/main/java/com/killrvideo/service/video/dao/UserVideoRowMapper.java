package com.killrvideo.service.video.dao;

import com.datastax.oss.driver.api.core.cql.Row;
import com.killrvideo.service.video.dto.UserVideo;
import org.springframework.stereotype.Component;

import static com.killrvideo.dse.dto.Video.*;

@Component
public class UserVideoRowMapper {
    public UserVideo map(Row row) {
        return new UserVideo(
                row.getUuid(COLUMN_USERID),
                row.getUuid(COLUMN_VIDEOID),
                row.getString(COLUMN_NAME),
                row.getString(COLUMN_PREVIEW),
                row.getInstant(COLUMN_ADDED_DATE)
        );
    }
}
