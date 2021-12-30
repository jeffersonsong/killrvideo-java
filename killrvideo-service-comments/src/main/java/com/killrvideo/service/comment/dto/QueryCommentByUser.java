package com.killrvideo.service.comment.dto;

import java.util.Optional;
import java.util.UUID;

import com.killrvideo.dse.dto.QueryDefinition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Query to search comments for a User.
 *
 * @author DataStax Developer Advocates team.
 */
@Getter @Setter @NoArgsConstructor
public class QueryCommentByUser extends QueryDefinition {
    private static final long serialVersionUID = -1182083603451480239L;

    /**
     * User unique identifier.
     */
    private UUID userId = null;

    /**
     * Comment offset if specified (Optional)
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<UUID> commentId = Optional.empty();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public QueryCommentByUser(UUID userId, Optional<UUID> commentId, int pageSize, Optional<String> pageState) {
        super(pageSize, pageState);
        this.userId = userId;
        this.commentId = commentId;
    }
}
