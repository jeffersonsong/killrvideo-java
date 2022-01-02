package com.killrvideo.service.comment.dao;

import com.datastax.oss.driver.api.core.cql.Row;
import com.killrvideo.service.comment.dto.Comment;
import org.springframework.stereotype.Component;

import static com.killrvideo.service.comment.dto.Comment.*;

@Component
public class CommentRowMapper {
    public Comment map(Row row) {
        Comment comment = new Comment();
        comment.setComment(row.getString(COLUMN_COMMENT));
        comment.setUserid(row.getUuid(COLUMN_USERID));
        comment.setCommentid(row.getUuid(COLUMN_COMMENTID));
        comment.setVideoid(row.getUuid(COLUMN_VIDEOID));
        comment.setDateOfComment(row.getInstant("comment_timestamp"));
        return comment;
    }
}
