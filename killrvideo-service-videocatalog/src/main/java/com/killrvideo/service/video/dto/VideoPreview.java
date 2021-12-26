package com.killrvideo.service.video.dto;

import java.time.Instant;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.killrvideo.dse.dto.AbstractVideo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pojo representing multiple videso.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter @Setter @NoArgsConstructor
public class VideoPreview extends AbstractVideo {
    private static final long serialVersionUID = 1319627901957309436L;

    @ClusteringColumn
    private Instant addedDate;

    @ClusteringColumn(1)
    private UUID videoid;

    /**
     * Constructor used by sub entities.
     */
    protected VideoPreview(String name, String preview, Instant addedDate, UUID videoid) {
        super(name, preview);
        this.addedDate = addedDate;
        this.videoid   = videoid;
    }
}
