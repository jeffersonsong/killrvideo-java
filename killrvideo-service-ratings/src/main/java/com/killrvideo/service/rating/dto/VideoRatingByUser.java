package com.killrvideo.service.rating.dto;

import java.io.Serializable;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

/**
 * Pojo representing DTO for table 'video_ratings_by_user'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("video_ratings_by_user")
public class VideoRatingByUser implements Serializable {

    /** Serial. */
    private static final long serialVersionUID = 7124040203261999049L;

    @PartitionKey
    private UUID videoid;

    @ClusteringColumn
    private UUID userid;

    private int rating;

    /**
     * Default constructor (reflection)
     */
    public VideoRatingByUser() {}

    /**
     * Constructor with all parameters.
     */
    public VideoRatingByUser(UUID videoid, UUID userid, int rating) {
        this.videoid = videoid;
        this.userid = userid;
        this.rating = rating;
    }

    /**
     * Getter for attribute 'videoid'.
     *
     * @return
     *       current value of 'videoid'
     */
    public UUID getVideoid() {
        return videoid;
    }

    /**
     * Setter for attribute 'videoid'.
     * @param videoid
     * 		new value for 'videoid '
     */
    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    /**
     * Getter for attribute 'userid'.
     *
     * @return
     *       current value of 'userid'
     */
    public UUID getUserid() {
        return userid;
    }

    /**
     * Setter for attribute 'userid'.
     * @param userid
     * 		new value for 'userid '
     */
    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    /**
     * Getter for attribute 'rating'.
     *
     * @return
     *       current value of 'rating'
     */
    public int getRating() {
        return rating;
    }

    /**
     * Setter for attribute 'rating'.
     * @param rating
     * 		new value for 'rating '
     */
    public void setRating(int rating) {
        this.rating = rating;
    }
    
}
