package com.killrvideo.messaging.utils

import com.killrvideo.messaging.utils.KafkaConsumerExtensions.receiveMessages
import com.killrvideo.utils.FormatUtils
import killrvideo.common.CommonEvents.ErrorEvent
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo
import killrvideo.user_management.events.UserManagementEvents.UserCreated
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.*
import java.util.concurrent.CountDownLatch

object SimpleKafkaConsumer {
    private const val KAFKA_SERVER_URL = "localhost:9092"
    private const val CONSUMER_GROUP = "killrvideo"
    private const val USER_CREATION_TOPIC = "topic-kv-userCreation"
    private const val VIDEO_CREATION_TOPIC = "topic-kv-videoCreation"
    private const val VIDEO_RATED_TOPIC = "topic-kv-videoRating"
    private const val ERROR_TOPIC = "topic-kv-errors"

    @JvmStatic
    fun main(args: Array<String>) {
        //create kafka consumer
        val userCreationConsumer: KafkaConsumer<String, ByteArray> = createConsumer()
        val videoCreationConsumer: KafkaConsumer<String, ByteArray> = createConsumer()
        val videoRatedConsumer: KafkaConsumer<String, ByteArray> = createConsumer()
        val errorLogger: KafkaConsumer<String, ByteArray> = createConsumer()

        Runtime.getRuntime().addShutdownHook(Thread {
            userCreationConsumer.close()
            videoCreationConsumer.close()
        })

        val latch = CountDownLatch(1)
        //subscribe to topic
        userCreationConsumer.receiveMessages(USER_CREATION_TOPIC) { record ->
            processUserCreationEvent(record)
        }
        videoCreationConsumer.receiveMessages(VIDEO_CREATION_TOPIC) { record ->
            processVideoCreationEvent(record)
        }
        videoRatedConsumer.receiveMessages(VIDEO_RATED_TOPIC) { record ->
            processVideoRatedEvent(record)
        }
        errorLogger.receiveMessages(ERROR_TOPIC) { record ->
            processErrorEvent(record)
        }
        try {
            latch.await()
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }

    private fun processUserCreationEvent(record: ConsumerRecord<String, ByteArray>) {
        val event = UserCreated.parseFrom(record.value())
        println("Message received from $USER_CREATION_TOPIC: ${FormatUtils.format(event)}")
    }

    private fun processVideoCreationEvent(record: ConsumerRecord<String, ByteArray>) {
        val event = YouTubeVideoAdded.parseFrom(record.value())
        println("Message received from $VIDEO_CREATION_TOPIC: ${FormatUtils.format(event)}")
    }

    private fun processVideoRatedEvent(record: ConsumerRecord<String, ByteArray>) {
        val event = UserRatedVideo.parseFrom(record.value())
        println("Message received from $VIDEO_RATED_TOPIC: ${FormatUtils.format(event)}")
    }

    private fun processErrorEvent(record: ConsumerRecord<String, ByteArray>) {
        val event = ErrorEvent.parseFrom(record.value())
        println("Message received from $ERROR_TOPIC: ${FormatUtils.format(event)}")
    }

    private fun createConsumer(): KafkaConsumer<String, ByteArray> {
        val properties = Properties()
        properties[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = KAFKA_SERVER_URL
        properties[ConsumerConfig.GROUP_ID_CONFIG] = CONSUMER_GROUP
        properties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        properties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
        properties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        properties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        return KafkaConsumer(properties)
    }
}
