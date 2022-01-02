package com.killrvideo.service.comment.dto

import com.datastax.oss.driver.api.mapper.annotations.*
import java.time.Instant
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Specialization for VIDEO.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("comments_by_video")
data class CommentByVideo(
    @PartitionKey
    var videoid: @NotNull UUID?,
    @ClusteringColumn
    var commentid: @NotNull UUID?,
    var comment: @Size(min = 1, message = "The comment must not be empty") String?,
    var userid: @NotNull UUID?,
    @Computed("toTimestamp(commentid)")
    var dateOfComment: @NotNull Instant?
) {
    fun toComment(): Comment =
        Comment(
            userid = this.userid,
            videoid = this.videoid,
            commentid = this.commentid,
            comment = this.comment,
            dateOfComment = this.dateOfComment
        )

    companion object {
        fun from(comment: Comment): CommentByVideo =
            CommentByVideo(
                userid = comment.userid,
                videoid = comment.videoid,
                commentid = comment.commentid,
                comment = comment.comment,
                dateOfComment = comment.dateOfComment
            )
    }
}
