package com.killrvideo.service.statistic.dto;

import java.io.Serializable;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

/**
 * Pojo representing DTO for table 'video_playback_stats'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("video_playback_stats")
public class VideoPlaybackStats implements Serializable {

    /** Serial. */
    private static final long serialVersionUID = -8636413035520458200L;
    
    /** COLUNMNS NAMES. */
    public static final String COLUMN_VIDEOID   = "videoid";
    public static final String COLUMN_VIEWS     = "views";

    @PartitionKey
    private UUID videoid;

    /**
     * "views" column is a counter type in the underlying DSE database.  As of driver version 3.2 there
     * is no "@Counter" annotation that I know of.  No worries though, just use the incr() function
     * while using the QueryBuilder.  Something similar to with(QueryBuilder.incr("views")).
     */
    private Long views;

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
     * Getter for attribute 'views'.
     *
     * @return
     *       current value of 'views'
     */
    public Long getViews() {
        return views;
    }

    /**
     * Setter for attribute 'views'.
     * @param views
     * 		new value for 'views '
     */
    public void setViews(Long views) {
        this.views = views;
    }
}
