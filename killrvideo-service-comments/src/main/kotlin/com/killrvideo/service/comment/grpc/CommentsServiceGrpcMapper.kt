package com.killrvideo.service.comment.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.comment.dto.Comment
import com.killrvideo.service.comment.dto.QueryCommentByUser
import com.killrvideo.service.comment.dto.QueryCommentByVideo
import com.killrvideo.utils.GrpcMappingUtils.*
import killrvideo.comments.CommentsServiceOuterClass.*
import killrvideo.comments.events.CommentsEvents.UserCommentedOnVideo
import killrvideo.comments.events.userCommentedOnVideo
import killrvideo.comments.getUserCommentsResponse
import killrvideo.comments.getVideoCommentsResponse
import killrvideo.comments.userComment
import killrvideo.comments.videoComment
import org.apache.commons.lang3.StringUtils.isBlank
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Validation of inputs and mapping
 *
 * @author DataStax Developer Advocates team.
 */
@Component
class CommentsServiceGrpcMapper {
    // --- Mappings ---
    object GetUserCommentsRequestExtensions {
        fun GetUserCommentsRequest.parse(): QueryCommentByUser =
            QueryCommentByUser(
                userId = fromUuid(this.userId),
                pageSize = this.pageSize,
                pageState = if (isNotBlank(this.pagingState)) this.pagingState else null,
                commentId = if (this.hasStartingCommentId() && !isBlank(this.startingCommentId.value))
                    fromTimeUuid(this.startingCommentId)
                else null
            )
    }

    object GetVideoCommentsRequestExtensions {
        fun GetVideoCommentsRequest.parse(): QueryCommentByVideo =
            QueryCommentByVideo(
                videoId = fromUuid(this.videoId),
                pageSize = this.pageSize,
                pageState = if (isNotBlank(this.pagingState))
                    this.pagingState
                else null,
                commentId = if (this.hasStartingCommentId() && !isBlank(this.startingCommentId.value))
                    fromTimeUuid(this.startingCommentId)
                else null
            )
    }

    object CommentOnVideoRequestExtensions {
        fun CommentOnVideoRequest.parse(): Comment =
            Comment(
                videoid = if (hasVideoId()) fromUuid(this.videoId) else null,
                commentid = if (hasCommentId()) fromTimeUuid(this.commentId) else null,
                userid = if (hasUserId()) fromUuid(this.userId) else null,
                comment = if (hasCommentId()) this.comment else null,
                dateOfComment = Instant.now()
            )
    }

    // Map from CommentDseDao response bean to expected GRPC object.
    fun mapFromDseVideoCommentToGrpcResponse(comments: ResultListPage<Comment>): GetVideoCommentsResponse =
        getVideoCommentsResponse {
            for (c in comments.results) {
                c.videoid?.let { videoId = uuidToUuid(it) }
                this.comments.add(newVideoCommentProto(c))
            }
            comments.pagingState.ifPresent {
                pagingState = it
            }
        }

    private fun newVideoCommentProto(c: Comment): VideoComment =
        videoComment {
            c.comment?.let { comment = it }
            c.userid?.let { userId = uuidToUuid(it) }
            c.commentid?.let { commentId = uuidToTimeUuid(it) }
            c.dateOfComment?.let { commentTimestamp = instantToTimeStamp(it) }
        }

    // Map from CommentDseDao response bean to expected GRPC object.
    fun mapFromDseUserCommentToGrpcResponse(dseRes: ResultListPage<Comment>): GetUserCommentsResponse =
        getUserCommentsResponse {
            for (c in dseRes.results) {
                c.userid?.let { userId = uuidToUuid(it) }
                comments.add(newUserCommentProto(c))
            }
            dseRes.pagingState.ifPresent { pagingState = it }
        }

    private fun newUserCommentProto(c: Comment): UserComment =
        userComment {
            c.comment?.let { comment = it }
            c.commentid?.let { commentId = uuidToTimeUuid(it) }
            c.videoid?.let { videoId = uuidToUuid(it) }
            c.dateOfComment?.let { commentTimestamp = instantToTimeStamp(it) }
        }

    fun createUserCommentedOnVideoEvent(comment: Comment): UserCommentedOnVideo =
        userCommentedOnVideo {
            comment.commentid?.let { commentId = uuidToTimeUuid(it) }
            comment.videoid?.let { videoId = uuidToUuid(it) }
            comment.userid?.let { userId = uuidToUuid(it) }
            comment.dateOfComment?.let { commentTimestamp = instantToTimeStamp(it) }
        }
}
