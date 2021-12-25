package com.killrvideo.service.comment.dto;

import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

/**
 * Specialization for USER.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("comments_by_user")
public class CommentByUser extends Comment {
    
    /** Serial. */
    private static final long serialVersionUID = 1453554109222565840L;
    
    /**
     * Default constructor.
     */
    public CommentByUser() {}
    
    /**
     * Copy constructor.
     *
     * @param c
     */
    public CommentByUser(Comment c) {
        this.commentid  = c.getCommentid();
        this.userid     = c.getUserid();
        this.videoid    = c.getVideoid();
        this.comment    = c.getComment();
    }

    /**
     * Getter for attribute 'userid'.
     *
     * @return
     *       current value of 'userid'
     */
    @PartitionKey
    public UUID getUserid() {
        return userid;
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
