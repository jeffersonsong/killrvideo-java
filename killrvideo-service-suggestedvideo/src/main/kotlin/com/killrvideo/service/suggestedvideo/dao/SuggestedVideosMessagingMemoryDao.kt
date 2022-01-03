package com.killrvideo.service.suggestedvideo.dao

import com.google.common.eventbus.Subscribe
import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo
import killrvideo.user_management.events.UserManagementEvents.UserCreated
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * With Guava subscription is done with annotation Subscribe. Event are sent in the
 * bus and correct method is invoked based on the event type (classname).
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Repository("killrvideo.rating.dao.messaging")
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_MEMORY)
class SuggestedVideosMessagingMemoryDao(
    suggestedVideosRepository: SuggestedVideosRepository,
    mapper: SuggestedVideosServiceGrpcMapper
) : SuggestedVideosMessagingDaoSupport(suggestedVideosRepository, mapper) {
    /**
     * {@inheritDoc}
     */
    @Subscribe
    public override fun onYoutubeVideoAddingMessage(youTubeVideoAdded: YouTubeVideoAdded) {
        super.onYoutubeVideoAddingMessage(youTubeVideoAdded)
    }

    /**
     * {@inheritDoc}
     */
    @Subscribe
    public override fun onUserCreatingMessage(userCreated: UserCreated) {
        super.onUserCreatingMessage(userCreated)
    }

    /**
     * {@inheritDoc}
     */
    @Subscribe
    public override fun onVideoRatingMessage(userRatedVideo: UserRatedVideo) {
        super.onVideoRatingMessage(userRatedVideo)
    }
}