package com.killrvideo.service.comment.dto;

import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.NoArgsConstructor;

/**
 * Specialization for VIDEO.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("comments_by_video")
@NoArgsConstructor
public class CommentByVideo extends Comment {
    private static final long serialVersionUID = -6738790629520080307L;

    public static CommentByVideo from(Comment comment) {
        return new CommentByVideo(comment);
    }
    
    private CommentByVideo(Comment c) {
        super(c);
    }

    @PartitionKey
    public UUID getVideoid() {
        return videoid;
    }

    public Comment toComment() {
        return new Comment(this);
    }
}
