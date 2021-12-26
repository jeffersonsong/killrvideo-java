package com.killrvideo.dse.dto;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.killrvideo.dse.utils.EmptyCollectionIfNull;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Pojo representing DTO for table 'videos'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("videos")
@Getter
public class Video extends AbstractVideo {
    private static final long serialVersionUID = 7035802926837646137L;

    /** Column names in the DB. */
    public static final String COLUMN_USERID       = "userid";
    public static final String COLUMN_VIDEOID      = "videoid";
    public static final String COLUMN_DESCRIPTION  = "description";
    public static final String COLUMN_LOCATION     = "location";
    public static final String COLUMN_LOCATIONTYPE = "location_type";
    public static final String COLUMN_ADDED_DATE   = "added_date";

    @PartitionKey
    private UUID videoid;

    @NotNull
    private UUID userid;

    @Length(min = 1, message = "description must not be empty")
    private String description;

    @Length(min = 1, message = "location must not be empty")
    private String location;

    private int locationType;

    @EmptyCollectionIfNull
    private Set<String> tags;

    @NotNull
    private Instant addedDate;

    /**
     * Default Constructor allowing reflection.
     */
    public Video() {}
    
    /**
     * Default Constructor allowing reflection.
     */
    public Video(String title) {
        this.name = title;
    }
    
    /**
     * Constructor wihout location nor preview.
     */
    public Video(UUID videoid, UUID userid, String name, String description, int locationType, Set<String> tags, Instant addedDate) {
        this(videoid, userid, name, description, null, locationType, null, tags, addedDate);
    }

    /**
     * All attributes constructor.
     */
    public Video(UUID videoid, UUID userid, String name, String description, String location, int locationType, String previewImageLocation, Set<String> tags, Instant addedDate) {
        super(name, previewImageLocation);
        this.videoid = videoid;
        this.userid = userid;
        this.description = description;
        this.location = location;
        this.locationType = locationType;
        this.tags = tags;
        this.addedDate = addedDate;
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
     * Getter for attribute 'description'.
     *
     * @return
     *       current value of 'description'
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for attribute 'description'.
     * @param description
     * 		new value for 'description '
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for attribute 'location'.
     *
     * @return
     *       current value of 'location'
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter for attribute 'location'.
     * @param location
     * 		new value for 'location '
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Getter for attribute 'locationType'.
     *
     * @return
     *       current value of 'locationType'
     */
    public int getLocationType() {
        return locationType;
    }

    /**
     * Setter for attribute 'locationType'.
     * @param locationType
     * 		new value for 'locationType '
     */
    public void setLocationType(int locationType) {
        this.locationType = locationType;
    }

    /**
     * Getter for attribute 'tags'.
     *
     * @return
     *       current value of 'tags'
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Setter for attribute 'tags'.
     * @param tags
     * 		new value for 'tags '
     */
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * Getter for attribute 'addedDate'.
     *
     * @return
     *       current value of 'addedDate'
     */
    public Instant getAddedDate() {
        return addedDate;
    }

    /**
     * Setter for attribute 'addedDate'.
     * @param addedDate
     * 		new value for 'addedDate '
     */
    public void setAddedDate(Instant addedDate) {
        this.addedDate = addedDate;
    }
}
