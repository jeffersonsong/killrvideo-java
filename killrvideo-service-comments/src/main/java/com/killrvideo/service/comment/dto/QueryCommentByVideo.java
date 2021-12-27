package com.killrvideo.service.comment.dto;

import com.killrvideo.dse.dto.QueryDefinition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

/**
 * Query to search comments for a User.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter
@Setter
@NoArgsConstructor
public class QueryCommentByVideo extends QueryDefinition {
    private static final long serialVersionUID = 7721676513515347779L;

    /**
     * Video unique identifier.
     */
    private UUID videoId = null;

    /**
     * Comment offset if specified (Optional)
     */
    private Optional<UUID> commentId = Optional.empty();
}
