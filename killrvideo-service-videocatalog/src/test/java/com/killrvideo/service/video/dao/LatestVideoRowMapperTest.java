package com.killrvideo.service.video.dao;

import com.datastax.oss.driver.api.core.cql.Row;
import com.killrvideo.service.video.dto.LatestVideo;
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

class LatestVideoRowMapperTest {
    private LatestVideoRowMapper mapper = new LatestVideoRowMapper();

    @Test
    public void testMap() {
        Row row = mock(Row.class);

        String yyyymmdd = "20211231";
        UUID userid = UUID.randomUUID();
        UUID videoid = UUID.randomUUID();
        String name = "Game";
        String previewLocation = "previewUrl";
        Instant addedDate = Instant.now();

        when(row.getString(COLUMN_YYYYMMDD)).thenReturn(yyyymmdd);
        when(row.getUuid(COLUMN_USERID)).thenReturn(userid);
        when(row.getUuid(COLUMN_VIDEOID)).thenReturn(videoid);
        when(row.getString(COLUMN_NAME)).thenReturn(name);
        when(row.getString(COLUMN_PREVIEW)).thenReturn(previewLocation);
        when(row.getInstant(COLUMN_ADDED_DATE)).thenReturn(addedDate);

        LatestVideo result = mapper.map(row);
        assertEquals(yyyymmdd, result.getYyyymmdd());
        assertEquals(userid, result.getUserid());
        assertEquals(videoid, result.getVideoid());
        assertEquals(name, result.getName());
        assertEquals(previewLocation, result.getPreviewImageLocation());
        assertEquals(addedDate, result.getAddedDate());
    }
}