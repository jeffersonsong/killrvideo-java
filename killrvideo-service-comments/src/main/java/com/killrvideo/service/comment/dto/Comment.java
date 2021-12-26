package com.killrvideo.service.comment.dto;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Computed;
import com.killrvideo.dse.dto.AbstractEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Bean standing for comment on video.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter @Setter @NoArgsConstructor
public class Comment extends AbstractEntity {
    private static final long serialVersionUID = 7675521710612951368L;

    /**
     * Column names in the DB.
     */
    public static final String COLUMN_USERID = "userid";
    public static final String COLUMN_VIDEOID = "videoid";
    public static final String COLUMN_COMMENTID = "commentid";
    public static final String COLUMN_COMMENT = "comment";

    @NotNull
    protected UUID userid;

    @NotNull
    protected UUID videoid;

    @NotNull
    @ClusteringColumn
    protected UUID commentid;

    @Length(min = 1, message = "The comment must not be empty")
    protected String comment;

    @NotNull
    @Computed("toTimestamp(commentid)")
    private Instant dateOfComment;

    /**
     * Copy constructor.
     *
     * @param other comment.
     */
    public Comment(Comment other) {
        this.commentid = other.commentid;
        this.userid = other.userid;
        this.videoid = other.videoid;
        this.comment = other.comment;
        this.dateOfComment = other.dateOfComment;
    }
}
