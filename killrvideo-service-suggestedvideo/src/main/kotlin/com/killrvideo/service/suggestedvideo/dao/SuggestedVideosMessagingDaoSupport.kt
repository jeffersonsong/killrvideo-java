package com.killrvideo.service.suggestedvideo.dao

import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository
import com.killrvideo.utils.GrpcMappingUtils
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo
import killrvideo.user_management.events.UserManagementEvents.UserCreated
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import org.slf4j.LoggerFactory

/**
 * Message processing for suggestion services.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
abstract class SuggestedVideosMessagingDaoSupport(
    protected val suggestedVideosRepository: SuggestedVideosRepository,
    private val mapper: SuggestedVideosServiceGrpcMapper
) {
    /**
     * Message is consumed from specialized class but treatment is the same, updating graph.
     *
     * @param userVideoRated
     * a user has rated a video event
     */
    protected open fun onVideoRatingMessage(userVideoRated: UserRatedVideo) {
        val videoId = userVideoRated.videoId.value
        val userId = GrpcMappingUtils.fromUuid(userVideoRated.userId)
        val rating = userVideoRated.rating
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("[NewUserEvent] Processing rating with user {} and video {}", userId, videoId)
        }
        suggestedVideosRepository.updateGraphNewUserRating(videoId, userId, rating)
    }

    /**
     * Message is consumed from specialized class but treatment is the same, updating graph.
     *
     * @param userCreationMessage
     * a user has been created
     */
    protected open fun onUserCreatingMessage(userCreationMessage: UserCreated) {
        val userId = GrpcMappingUtils.fromUuid(userCreationMessage.userId)
        val userCreation = GrpcMappingUtils.timestampToDate(userCreationMessage.timestamp)
        val email = userCreationMessage.email
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("[NewUserEvent] Processing for user {} ", userId)
        }
        suggestedVideosRepository.updateGraphNewUser(userId, email, userCreation)
    }

    /**
     * Message is consumed from specialized class but treatment is the same, updating graph.
     *
     * @param videoAdded
     * a video has been created
     */
    protected open fun onYoutubeVideoAddingMessage(videoAdded: YouTubeVideoAdded) {
        suggestedVideosRepository.updateGraphNewVideo(mapper.mapVideoAddedtoVideoDTO(videoAdded))
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SuggestedVideosMessagingDaoSupport::class.java)
    }
}
