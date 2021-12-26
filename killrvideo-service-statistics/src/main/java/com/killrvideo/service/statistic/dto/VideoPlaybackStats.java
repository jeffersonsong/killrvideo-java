package com.killrvideo.service.statistic.dto;

import java.io.Serializable;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pojo representing DTO for table 'video_playback_stats'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("video_playback_stats")
@Getter
@Setter
@NoArgsConstructor
public class VideoPlaybackStats implements Serializable {
    private static final long serialVersionUID = -8636413035520458200L;

    @PartitionKey
    private UUID videoid;

    /**
     * "views" column is a counter type in the underlying DSE database.  As of driver version 3.2 there
     * is no "@Counter" annotation that I know of.  No worries though, just use the incr() function
     * while using the QueryBuilder.  Something similar to with(QueryBuilder.incr("views")).
     */
    private Long views;
}
