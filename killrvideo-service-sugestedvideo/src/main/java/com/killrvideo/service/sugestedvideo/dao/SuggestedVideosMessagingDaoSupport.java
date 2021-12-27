package com.killrvideo.service.sugestedvideo.dao;

import java.util.Date;
import java.util.UUID;

import com.killrvideo.service.sugestedvideo.grpc.SuggestedVideosServiceGrpcMapper;
import com.killrvideo.service.sugestedvideo.repository.SuggestedVideosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.killrvideo.utils.GrpcMappingUtils;

import killrvideo.ratings.events.RatingsEvents.UserRatedVideo;
import killrvideo.user_management.events.UserManagementEvents.UserCreated;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;

import javax.inject.Inject;

/**
 * Message processing for suggestion services.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class SuggestedVideosMessagingDaoSupport {
    
    /** Loger for that class. */
    private static Logger LOGGER = LoggerFactory.getLogger(SuggestedVideosMessagingDaoSupport.class);
    
    @Inject
    protected SuggestedVideosRepository suggestedVideosRepository;
    @Inject
    private SuggestedVideosServiceGrpcMapper mapper;

    /**
     * Message is consumed from specialized class but treatment is the same, updating graph.
     * 
     * @param userVideoRated
     *      a user has rated a video event
     */
    protected void onVideoRatingMessage(UserRatedVideo userVideoRated) {
        String videoId = userVideoRated.getVideoId().getValue();
        UUID   userId  = UUID.fromString(userVideoRated.getUserId().getValue());
        int rating     = userVideoRated.getRating();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[NewUserEvent] Processing rating with user {} and video {}", userId, videoId);
        }
        suggestedVideosRepository.updateGraphNewUserRating(videoId, userId, rating);
    }
    
    /**
     * Message is consumed from specialized class but treatment is the same, updating graph.
     * 
     * @param userCreationMessage
     *      a user has been created
     */
    protected void onUserCreatingMessage(UserCreated userCreationMessage) {
        final UUID userId       = UUID.fromString(userCreationMessage.getUserId().getValue());
        final Date userCreation = GrpcMappingUtils.timestampToDate(userCreationMessage.getTimestamp());
        final String email      = userCreationMessage.getEmail();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[NewUserEvent] Processing for user {} ", userId);
        }
        suggestedVideosRepository.updateGraphNewUser(userId, email, userCreation);
    }
    
    /**
     * Message is consumed from specialized class but treatment is the same, updating graph.
     * 
     * @param videoAdded
     *      a video has been created
     */
    protected void onYoutubeVideoAddingMessage(YouTubeVideoAdded videoAdded) {
       suggestedVideosRepository.updateGraphNewVideo(mapper.mapVideoAddedtoVideoDTO(videoAdded));
    }
    

}
