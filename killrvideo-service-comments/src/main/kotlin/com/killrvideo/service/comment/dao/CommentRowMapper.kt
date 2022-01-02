package com.killrvideo.service.comment.dao

import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.service.comment.dto.Comment
import org.springframework.stereotype.Component

@Component
class CommentRowMapper {
    fun map(row: Row): Comment =
        Comment(
            comment = row.getString(Comment.COLUMN_COMMENT),
            userid = row.getUuid(Comment.COLUMN_USERID),
            commentid = row.getUuid(Comment.COLUMN_COMMENTID),
            videoid = row.getUuid(Comment.COLUMN_VIDEOID),
            dateOfComment = row.getInstant(Comment.COLUMN_COMMENT_TIMESTAMP)
        )
}
