package com.killrvideo.service.comment.dto

import java.util.*

/**
 * Query to search comments for a User.
 *
 * @author DataStax Developer Advocates team.
 */
data class QueryCommentByVideo(
    val videoId: UUID,
    val commentId: UUID?,
    val pageSize: Int,
    val pageState: String?
)
