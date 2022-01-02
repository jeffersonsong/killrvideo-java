package com.killrvideo.service.comment.dto

import java.time.Instant
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Bean standing for comment on video.
 *
 * @author DataStax Developer Advocates team.
 */
data class Comment(
    var userid: @NotNull UUID?,
    var videoid: @NotNull UUID?,
    var commentid: @NotNull UUID?,
    var comment: @Size(min = 1, message = "The comment must not be empty") String?,
    var dateOfComment: @NotNull Instant?
) {
    /**
     * Copy constructor.
     *
     * @param other comment.
     */
    constructor(other: Comment) : this(
        userid = other.userid,
        videoid = other.videoid,
        commentid = other.commentid,
        comment = other.comment,
        dateOfComment = other.dateOfComment
    )

    companion object {
        /**
         * Column names in the DB.
         */
        const val COLUMN_USERID = "userid"
        const val COLUMN_VIDEOID = "videoid"
        const val COLUMN_COMMENTID = "commentid"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_COMMENT_TIMESTAMP = "comment_timestamp"
    }
}
