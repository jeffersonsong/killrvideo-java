package com.killrvideo.service.comment.dao;

import com.datastax.oss.driver.api.core.cql.Row;
import com.killrvideo.service.comment.dto.Comment;
import org.springframework.stereotype.Component;

import static com.killrvideo.service.comment.dto.Comment.*;

@Component
public class CommentRowMapper {
    public Comment map(Row row) {
        Comment c = new Comment();
        c.setComment(row.getString(COLUMN_COMMENT));
        c.setUserid(row.getUuid(COLUMN_USERID));
        c.setCommentid(row.getUuid(COLUMN_COMMENTID));
        c.setVideoid(row.getUuid(COLUMN_VIDEOID));
        c.setDateOfComment(row.getInstant("comment_timestamp"));
        return c;
    }
}
