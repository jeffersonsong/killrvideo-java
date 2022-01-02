package com.killrvideo.service.suggestedvideo.dto;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.killrvideo.dse.dto.AbstractVideo;
import com.killrvideo.dse.utils.EmptyCollectionIfNull;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
@Getter @Setter
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

    @Size(min = 1, message = "description must not be empty")
    private String description;

    @Size(min = 1, message = "location must not be empty")
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
}
