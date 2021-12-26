package com.killrvideo.service.rating.dto;

import java.io.Serializable;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pojo representing DTO for table 'video_ratings_by_user'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("video_ratings_by_user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VideoRatingByUser implements Serializable {
    private static final long serialVersionUID = 7124040203261999049L;

    @PartitionKey
    private UUID videoid;

    @ClusteringColumn
    private UUID userid;

    private int rating;
}
