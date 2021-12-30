package com.killrvideo.service.video.dao;

import com.datastax.oss.driver.api.core.cql.Row;
import com.killrvideo.service.video.dto.UserVideo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static com.killrvideo.dse.dto.AbstractVideo.COLUMN_NAME;
import static com.killrvideo.dse.dto.AbstractVideo.COLUMN_PREVIEW;
import static com.killrvideo.dse.dto.Video.*;
import static com.killrvideo.service.video.dto.LatestVideo.COLUMN_YYYYMMDD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserVideoRowMapperTest {
    private UserVideoRowMapper mapper = new UserVideoRowMapper();

    @Test
    public void testMap() {
        Row row = mock(Row.class);

        UUID userid = UUID.randomUUID();
        UUID videoid = UUID.randomUUID();
        String name = "Game";
        String previewLocation = "previewUrl";
        Instant addedDate = Instant.now();

        when(row.getUuid(COLUMN_USERID)).thenReturn(userid);
        when(row.getUuid(COLUMN_VIDEOID)).thenReturn(videoid);
        when(row.getString(COLUMN_NAME)).thenReturn(name);
        when(row.getString(COLUMN_PREVIEW)).thenReturn(previewLocation);
        when(row.getInstant(COLUMN_ADDED_DATE)).thenReturn(addedDate);

        UserVideo result = mapper.map(row);
        assertEquals(userid, result.getUserid());
        assertEquals(videoid, result.getVideoid());
        assertEquals(name, result.getName());
        assertEquals(previewLocation, result.getPreviewImageLocation());
        assertEquals(addedDate, result.getAddedDate());
    }
}