package com.killrvideo.service.rating.dto;

import java.io.Serializable;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.Getter;
import lombok.Setter;

/**
 * Pojo representing DTO for table 'video_ratings'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("video_ratings")
@Getter @Setter
public class VideoRating implements Serializable {
    private static final long serialVersionUID = -8874199914791405808L;

    @PartitionKey
    private UUID videoid;

    private Long ratingCounter;

    private Long ratingTotal;
}
