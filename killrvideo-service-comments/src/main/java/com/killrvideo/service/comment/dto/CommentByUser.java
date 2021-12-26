package com.killrvideo.service.comment.dto;

import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.NoArgsConstructor;

/**
 * Specialization for USER.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("comments_by_user")
@NoArgsConstructor
public class CommentByUser extends Comment {
    private static final long serialVersionUID = 1453554109222565840L;

    public static CommentByUser from(Comment comment) {
        return new CommentByUser(comment);
    }

    private CommentByUser(Comment c) {
        super(c);
    }

    @PartitionKey
    public UUID getUserid() {
        return userid;
    }

    public Comment toComment() {
        return new Comment(this);
    }
}
