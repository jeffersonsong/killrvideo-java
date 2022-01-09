package com.killrvideo.service.suggestedvideo.dao

import com.google.protobuf.InvalidProtocolBufferException
import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.messaging.utils.KafkaConsumerExtensions.receiveMessages
import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo
import killrvideo.user_management.events.UserManagementEvents.UserCreated
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Repository("killrvideo.rating.dao.messaging")
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_KAFKA)
class SuggestedVideosMessagingKafkaDao(
    suggestedVideosRepository: SuggestedVideosRepository,
    mapper: SuggestedVideosServiceGrpcMapper,
    // --------------------------------------------------------------------------
    // -------------------------- User Creation ---------------------------------
    // --------------------------------------------------------------------------
    @Qualifier("kafka.consumer.userCreating") private val consumerUserCreatedProtobuf: KafkaConsumer<String, ByteArray>,
    @Qualifier("kafka.consumer.videoCreating") private val consumerVideoCreatedProtobuf: KafkaConsumer<String, ByteArray>,
    @Qualifier("kafka.consumer.videoRating") private val consumerVideoRatingProtobuf: KafkaConsumer<String, ByteArray>,
    @Value("\${killrvideo.messaging.destination.userCreated:topic-kv-userCreation}")
    private val topicUserCreated: String,
    @Value("\${killrvideo.messaging.destination.youTubeVideoAdded:topic-kv-videoCreation}")
    private val topicVideoCreated: String,
    @Value("\${killrvideo.messaging.destination.videoRated:topic-kv-videoRating}")
    private val topicVideoRated: String
) : SuggestedVideosMessagingDaoSupport(suggestedVideosRepository, mapper) {
    private val logger = KotlinLogging.logger { }
    private val executor = Executors.newFixedThreadPool(2)

    @PostConstruct
    fun registerConsumers() {
        registerConsumerUserCreated()
        registerConsumerYoutubeVideoAdded()
        registerConsumerVideoRating()
    }

    private fun registerConsumerUserCreated() {
        consumerUserCreatedProtobuf.receiveMessages(
            topicUserCreated, Duration.ofMillis(100L)
        ) { record ->
            executor.submit {
                parseUserCreatedMessage(record.value())
            }
        }
    }

    private fun parseUserCreatedMessage(payload: ByteArray?) {
        try {
            super.onUserCreatingMessage(UserCreated.parseFrom(payload))
            logger.info { "Received event from $topicUserCreated" }
        } catch (e: InvalidProtocolBufferException) {
            logger.error("Cannot parse message expecting object " + UserCreated::class.java.name, e)
        }
    }

    // --------------------------------------------------------------------------
    // -------------------------- Video Creation --------------------------------
    // --------------------------------------------------------------------------

    private fun registerConsumerYoutubeVideoAdded() {
        consumerVideoCreatedProtobuf.receiveMessages(
            topicVideoCreated, Duration.ofMillis(100L)
        ) { record ->
            executor.submit {
                parseYoutubeVideoAddedMessage(record.value())
            }
        }
    }

    private fun parseYoutubeVideoAddedMessage(payload: ByteArray) {
        try {
            // Marshall binary to Protobuf Stub
            super.onYoutubeVideoAddingMessage(YouTubeVideoAdded.parseFrom(payload))
            logger.info { "Received event from $topicVideoCreated" }
        } catch (e: InvalidProtocolBufferException) {
            logger.error("Cannot parse message expecting object " + UserCreated::class.java.name, e)
        }
    }

    // --------------------------------------------------------------------------
    // -------------------------- Video Rating --------------------------------
    // --------------------------------------------------------------------------
    private fun registerConsumerVideoRating() {
        consumerVideoRatingProtobuf.receiveMessages(
            topicVideoRated, Duration.ofMillis(100L)
        ) { record ->
            parseVideoRatingMessage(record.value())
        }
    }

    private fun parseVideoRatingMessage(payload: ByteArray) {
        try {
            super.onVideoRatingMessage(UserRatedVideo.parseFrom(payload))
            logger.info { "Received event from $topicVideoRated" }
        } catch (e: InvalidProtocolBufferException) {
            logger.error("Cannot parse message expecting object " + UserCreated::class.java.name, e)
        }
    }
}
