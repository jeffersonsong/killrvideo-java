package com.killrvideo.service.video.dto;

import java.time.Instant;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
/**
 * Pojo representing DTO for table 'user_videos'
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("user_videos")
public class UserVideo extends VideoPreview {

    /** Serial. */
    private static final long serialVersionUID = -4689177834790056936L;
    
    /** Column names in the DB. */
    public static final String COLUMN_USERID = "userid";
    
    @PartitionKey
    private UUID userid;

    /**
     * Deafult Constructor allowing reflection.
     */
    public UserVideo() {}

    /**
     * Constructor without preview.
     */
    public UserVideo(UUID userid, UUID videoid, String name, Instant addedDate) {
        this(userid, videoid, name, null, addedDate);
    }

    /**
     * Full set constructor.
     */
    public UserVideo(UUID userid, UUID videoid, String name, String previewImageLocation, Instant addedDate) {
        super(name, previewImageLocation, addedDate, videoid);
        this.userid = userid;
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
}
