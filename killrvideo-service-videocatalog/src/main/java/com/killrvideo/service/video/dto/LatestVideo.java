package com.killrvideo.service.video.dto;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.killrvideo.dse.dto.Video;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pojo representing DTO for table 'latest_videos'
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("latest_videos")
@Getter @Setter @NoArgsConstructor
public class LatestVideo extends VideoPreview {
    private static final long serialVersionUID = -8527565276521920973L;
    private static final DateTimeFormatter DATEFORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC));

    /** Column names in the DB. */
    public static final String COLUMN_YYYYMMDD = "yyyymmdd";
    
    @PartitionKey
    private String yyyymmdd;
    private UUID userid;

    public static LatestVideo from(Video v, Instant now) {
        String yyyyMMdd = DATEFORMATTER.format(now);
        return new LatestVideo(
                yyyyMMdd,
                v.getUserid(),
                v.getVideoid(),
                v.getName(),
                v.getPreviewImageLocation(),
                now
        );
    }

    /**
     * Constructor with all parameters.
     */
    public LatestVideo(String yyyymmdd, UUID userid, UUID videoid, String name, String previewImageLocation, Instant addedDate) {
        super(name, previewImageLocation, addedDate, videoid);
        this.yyyymmdd = yyyymmdd;
        this.userid = userid;
    }
}
