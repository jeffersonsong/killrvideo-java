package com.killrvideo.service.video.dto;

import java.time.Instant;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.killrvideo.dse.dto.Video;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pojo representing DTO for table 'user_videos'
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("user_videos")
@Getter @Setter @NoArgsConstructor
public class UserVideo extends VideoPreview {
    private static final long serialVersionUID = -4689177834790056936L;

    @PartitionKey
    private UUID userid;

    public static UserVideo from(Video v, Instant now) {
        return new UserVideo(
                v.getUserid(),
                v.getVideoid(),
                v.getName(),
                v.getPreviewImageLocation(),
                now
        );
    }

    /**
     * Full set constructor.
     */
    public UserVideo(UUID userid, UUID videoid, String name, String previewImageLocation, Instant addedDate) {
        super(name, previewImageLocation, addedDate, videoid);
        this.userid = userid;
    }
}
