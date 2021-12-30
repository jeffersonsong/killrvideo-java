package com.killrvideo.service.comment.dao;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.killrvideo.service.comment.dto.Comment;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static com.killrvideo.dse.dto.Video.*;
import static com.killrvideo.service.comment.dto.Comment.COLUMN_COMMENT;
import static com.killrvideo.service.comment.dto.Comment.COLUMN_COMMENTID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommentRowMapperTest {
    private CommentRowMapper mapper = new CommentRowMapper();

    @Test
    public void testMap() {
        Row row = mock(Row.class);

        String comment = "Comment";
        UUID userid = UUID.randomUUID();
        UUID videoid = UUID.randomUUID();
        UUID commentid = Uuids.timeBased();
        Instant dateOfComment = Instant.now();

        when(row.getString(COLUMN_COMMENT)).thenReturn(comment);
        when(row.getUuid(COLUMN_USERID)).thenReturn(userid);
        when(row.getUuid(COLUMN_COMMENTID)).thenReturn(commentid);
        when(row.getUuid(COLUMN_VIDEOID)).thenReturn(videoid);
        when(row.getInstant("comment_timestamp")).thenReturn(dateOfComment);

        Comment result = mapper.map(row);
        assertEquals(comment, result.getComment());
        assertEquals(userid, result.getUserid());
        assertEquals(commentid, result.getCommentid());
        assertEquals(videoid, result.getVideoid());
        assertEquals(dateOfComment, result.getDateOfComment());
    }
}