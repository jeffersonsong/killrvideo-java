package com.killrvideo.service.suggestedvideo.dao

import com.google.protobuf.InvalidProtocolBufferException
import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo
import killrvideo.user_management.events.UserManagementEvents.UserCreated
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.stream.StreamSupport
import javax.annotation.PostConstruct
import javax.inject.Inject

@Repository("killrvideo.rating.dao.messaging")
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_KAFKA)
class SuggestedVideosMessagingKafkaDao(
    suggestedVideosRepository: SuggestedVideosRepository,
    mapper: SuggestedVideosServiceGrpcMapper,
    // --------------------------------------------------------------------------
    // -------------------------- User Creation ---------------------------------
    // --------------------------------------------------------------------------
    @Qualifier("kafka.consumer.userCreating") private val consumerUserCreatedProtobuf: KafkaConsumer<String, ByteArray>,
    @Value("\${killrvideo.messaging.destination.userCreated : topic-kv-userCreation}")
    private val topicUserCreated: String,
    @Value("\${killrvideo.messaging.destination.youTubeVideoAdded : topic-kv-videoCreation}")
    private val topicVideoCreated: String,
    @Value("\${killrvideo.messaging.destination.videoRated : topic-kv-videoRating}")
    private val topicVideoRated: String
) : SuggestedVideosMessagingDaoSupport(suggestedVideosRepository, mapper) {

    @PostConstruct
    fun registerConsumerUserCreated() {
        LOGGER.info("Start consuming events from topic '{}' ..", topicUserCreated)
        consumerUserCreatedProtobuf.subscribe(listOf(topicUserCreated))
        StreamSupport.stream(consumerUserCreatedProtobuf.poll(Duration.ofSeconds(2L)).spliterator(), false)
            .map { obj: ConsumerRecord<String, ByteArray> -> obj.value() }
            .forEach { payload: ByteArray? -> parseUserCreatedMessage(payload) }
    }

    fun parseUserCreatedMessage(payload: ByteArray?) {
        try {
            super.onUserCreatingMessage(UserCreated.parseFrom(payload))
        } catch (e: InvalidProtocolBufferException) {
            LOGGER.error("Cannot parse message expecting object " + UserCreated::class.java.name, e)
        }
    }

    // --------------------------------------------------------------------------
    // -------------------------- Video Creation --------------------------------
    // --------------------------------------------------------------------------


    @Inject
    @Qualifier("kafka.consumer.videoCreating")
    private val consumerVideoCreatedProtobuf: KafkaConsumer<String, ByteArray>? = null
    @PostConstruct
    fun registerConsumerYoutubeVideoAdded() {
        LOGGER.info("Start consuming events from topic '{}' ..", topicVideoCreated)
        consumerVideoCreatedProtobuf!!.subscribe(listOf(topicVideoCreated))
        StreamSupport.stream(consumerVideoCreatedProtobuf.poll(Duration.ofSeconds(2L)).spliterator(), false)
            .map { obj: ConsumerRecord<String, ByteArray> -> obj.value() }
            .forEach { payload: ByteArray -> parseYoutubeVideoAddedMessage(payload) }
    }

    private fun parseYoutubeVideoAddedMessage(payload: ByteArray) {
        try {
            // Marshall binary to Protobuf Stub
            super.onYoutubeVideoAddingMessage(YouTubeVideoAdded.parseFrom(payload))
        } catch (e: InvalidProtocolBufferException) {
            LOGGER.error("Cannot parse message expecting object " + UserCreated::class.java.name, e)
        }
    }

    // --------------------------------------------------------------------------
    // -------------------------- Video Rating --------------------------------
    // --------------------------------------------------------------------------


    @Inject
    @Qualifier("kafka.consumer.videoRating")
    private val consumerVideoRatingProtobuf: KafkaConsumer<String, ByteArray>? = null
    @PostConstruct
    fun registerConsumerVideoRating() {
        LOGGER.info("Start consuming events from topic '{}' ..", topicVideoRated)
        consumerVideoRatingProtobuf!!.subscribe(listOf(topicVideoRated))
        StreamSupport.stream(consumerVideoRatingProtobuf.poll(Duration.ofSeconds(2L)).spliterator(), false)
            .map { obj: ConsumerRecord<String, ByteArray> -> obj.value() }
            .forEach { payload: ByteArray -> parseVideoRatingMessage(payload) }
    }

    private fun parseVideoRatingMessage(payload: ByteArray) {
        try {
            super.onVideoRatingMessage(UserRatedVideo.parseFrom(payload))
        } catch (e: InvalidProtocolBufferException) {
            LOGGER.error("Cannot parse message expecting object " + UserCreated::class.java.name, e)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SuggestedVideosMessagingKafkaDao::class.java)
    }
}
