package com.killrvideo.service.comment.dto;

import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

/**
 * Specialization for VIDEO.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("comments_by_video")
public class CommentByVideo extends Comment {
    
    /** Serial. */
    private static final long serialVersionUID = -6738790629520080307L;
    
    public CommentByVideo() {
    }
    
    public CommentByVideo(Comment c) {
        this.commentid  = c.getCommentid();
        this.userid     = c.getUserid();
        this.videoid    = c.getVideoid();
        this.comment    = c.getComment();
    }

    /**
     * Getter for attribute 'videoid'.
     *
     * @return
     *       current value of 'videoid'
     */
    @PartitionKey
    public UUID getVideoid() {
        return videoid;
    }

    public Comment toComment() {
        Comment comment = new Comment();
        comment.setCommentid(this.commentid);
        comment.setUserid(this.userid);
        comment.setVideoid(this.videoid);
        comment.setComment(this.comment);
        return comment;
    }
}
