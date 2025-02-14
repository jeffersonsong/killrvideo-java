package com.killrvideo.service.suggestedvideo.dao;

import com.google.protobuf.InvalidProtocolBufferException;
import com.killrvideo.conf.KillrVideoConfiguration;
import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper;
import com.killrvideo.service.suggestedvideo.repository.SuggestedVideosRepository;
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo;
import killrvideo.user_management.events.UserManagementEvents.UserCreated;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Collections;
import java.util.stream.StreamSupport;

@Repository("killrvideo.rating.dao.messaging")
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_KAFKA)
public class SuggestedVideosMessagingKafkaDao extends SuggestedVideosMessagingDaoSupport {

    /**
     * Loger for that class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestedVideosMessagingKafkaDao.class);

    // --------------------------------------------------------------------------
    // -------------------------- User Creation ---------------------------------
    // --------------------------------------------------------------------------

    private final KafkaConsumer<String, byte[]> consumerUserCreatedProtobuf;

    @Value("${killrvideo.messaging.destination.userCreated : topic-kv-userCreation}")
    private String topicUserCreated;

    public SuggestedVideosMessagingKafkaDao(
            SuggestedVideosRepository suggestedVideosRepository,
            SuggestedVideosServiceGrpcMapper mapper,
            @Qualifier("kafka.consumer.userCreating") KafkaConsumer<String, byte[]> consumerUserCreatedProtobuf) {
        super(suggestedVideosRepository, mapper);
        this.consumerUserCreatedProtobuf = consumerUserCreatedProtobuf;
    }

    @PostConstruct
    public void registerConsumerUserCreated() {
        LOGGER.info("Start consuming events from topic '{}' ..", topicUserCreated);
        consumerUserCreatedProtobuf.subscribe(Collections.singletonList(topicUserCreated));
        StreamSupport.stream(consumerUserCreatedProtobuf.poll(Duration.ofSeconds(2L)).spliterator(), false)
                .map(ConsumerRecord::value)
                .forEach(this::parseUserCreatedMessage);
    }

    public void parseUserCreatedMessage(byte[] payload) {
        try {
            super.onUserCreatingMessage(UserCreated.parseFrom(payload));
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Cannot parse message expecting object " + UserCreated.class.getName(), e);
        }
    }

    // --------------------------------------------------------------------------
    // -------------------------- Video Creation --------------------------------
    // --------------------------------------------------------------------------

    @Value("${killrvideo.messaging.destination.youTubeVideoAdded : topic-kv-videoCreation}")
    private String topicVideoCreated;

    @Inject
    @Qualifier("kafka.consumer.videoCreating")
    private KafkaConsumer<String, byte[]> consumerVideoCreatedProtobuf;

    @PostConstruct
    public void registerConsumerYoutubeVideoAdded() {
        LOGGER.info("Start consuming events from topic '{}' ..", topicVideoCreated);
        consumerVideoCreatedProtobuf.subscribe(Collections.singletonList(topicVideoCreated));
        StreamSupport.stream(consumerVideoCreatedProtobuf.poll(Duration.ofSeconds(2L)).spliterator(), false)
                .map(ConsumerRecord::value)
                .forEach(this::parseYoutubeVideoAddedMessage);
    }

    private void parseYoutubeVideoAddedMessage(byte[] payload) {
        try {
            // Marshall binary to Protobuf Stub
            super.onYoutubeVideoAddingMessage(YouTubeVideoAdded.parseFrom(payload));
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Cannot parse message expecting object " + UserCreated.class.getName(), e);
        }
    }


    // --------------------------------------------------------------------------
    // -------------------------- Video Rating --------------------------------
    // --------------------------------------------------------------------------

    @Value("${killrvideo.messaging.destination.videoRated : topic-kv-videoRating}")
    private String topicVideoRated;

    @Inject
    @Qualifier("kafka.consumer.videoRating")
    private KafkaConsumer<String, byte[]> consumerVideoRatingProtobuf;

    @PostConstruct
    public void registerConsumerVideoRating() {
        LOGGER.info("Start consuming events from topic '{}' ..", topicVideoRated);
        consumerVideoRatingProtobuf.subscribe(Collections.singletonList(topicVideoRated));
        StreamSupport.stream(consumerVideoRatingProtobuf.poll(Duration.ofSeconds(2L)).spliterator(), false)
                .map(ConsumerRecord::value)
                .forEach(this::parseVideoRatingMessage);
    }

    private void parseVideoRatingMessage(byte[] payload) {
        try {
            super.onVideoRatingMessage(UserRatedVideo.parseFrom(payload));
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Cannot parse message expecting object " + UserCreated.class.getName(), e);
        }
    }
}
