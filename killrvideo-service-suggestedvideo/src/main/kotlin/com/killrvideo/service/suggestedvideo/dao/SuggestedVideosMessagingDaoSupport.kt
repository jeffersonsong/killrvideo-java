package com.killrvideo.service.suggestedvideo.dao

import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import com.killrvideo.utils.GrpcMappingUtils.timestampToDate
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo
import killrvideo.user_management.events.UserManagementEvents.UserCreated
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import mu.KotlinLogging

/**
 * Message processing for suggestion services.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
abstract class SuggestedVideosMessagingDaoSupport(
    private val suggestedVideosRepository: SuggestedVideosRepository,
    private val mapper: SuggestedVideosServiceGrpcMapper
) {
    private val logger = KotlinLogging.logger { }

    /**
     * Message is consumed from specialized class but treatment is the same, updating graph.
     *
     * @param userVideoRated
     * a user has rated a video event
     */
    protected open fun onVideoRatingMessage(userVideoRated: UserRatedVideo) {
        val videoId = userVideoRated.videoId.value
        val userId = fromUuid(userVideoRated.userId)
        val rating = userVideoRated.rating
        logger.debug { "[NewUserEvent] Processing rating with user $userId and video $videoId" }
        suggestedVideosRepository.updateGraphNewUserRating(videoId, userId, rating)
    }

    /**
     * Message is consumed from specialized class but treatment is the same, updating graph.
     *
     * @param userCreationMessage
     * a user has been created
     */
    protected open fun onUserCreatingMessage(userCreationMessage: UserCreated) {
        val userId = fromUuid(userCreationMessage.userId)
        val userCreation = timestampToDate(userCreationMessage.timestamp)
        val email = userCreationMessage.email
        logger.debug {"[NewUserEvent] Processing for user $userId " }
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
}
