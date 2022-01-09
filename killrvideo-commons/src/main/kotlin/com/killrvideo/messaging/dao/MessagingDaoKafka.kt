package com.killrvideo.messaging.dao

import com.google.protobuf.AbstractMessageLite
import com.google.protobuf.InvalidProtocolBufferException
import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.messaging.utils.KafkaConsumerExtensions.receiveMessages
import com.killrvideo.messaging.utils.KafkaProducerExtensions.sendAsync
import killrvideo.common.CommonEvents.ErrorEvent
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.CompletableFuture
import javax.annotation.PostConstruct
import javax.inject.Inject

/**
 * Common Kafka message handler.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Repository("killrvideo.dao.messaging.kafka")
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_KAFKA)
class MessagingDaoKafka : MessagingDao {
    private val logger = KotlinLogging.logger {}

    /**
     * Same producer can be used evrytime (as the topicName is stored in [ProducerRecord].)
     */
    @Inject
    private lateinit var protobufProducer: KafkaProducer<String, ByteArray>

    /**
     * Common error processing from topic topic-kv-errors.
     */
    @Inject
    @Qualifier("kafka.consumer.error")
    private lateinit var errorLogger: KafkaConsumer<String, ByteArray>

    @Inject
    private lateinit var errorProcessor: ErrorProcessor
    /**
     * {@inheritDoc}
     */
    /**
     * Error Topic.
     */
    @Value("\${killrvideo.messaging.topics.errors:topic-kv-errors}")
    override lateinit var errorDestination: String

    /**
     * {@inheritDoc}
     */
    override fun sendEvent(targetDestination: String, event: Any): CompletableFuture<RecordMetadata> {
        logger.info("Sending Event '{}' ..", event.javaClass.name)
        val payload = serializePayload<Any?>(event)
        return protobufProducer.sendAsync(
            ProducerRecord(targetDestination, payload)
        )
    }

    // -- Common Error Handling --
    @PostConstruct
    fun registerErrorConsumer() {
        logger.info("Start consuming events from topic '{}' ..", errorDestination)
        errorLogger.receiveMessages(errorDestination) { record ->
            consumeErrorEvent(record.value())
        }
    }

    /**
     * Reading topic Error so expecting
     *
     * @param eventErrorPayload
     */
    private fun consumeErrorEvent(eventErrorPayload: ByteArray) {
        try {
            errorProcessor.handle(ErrorEvent.parseFrom(eventErrorPayload))
        } catch (e: InvalidProtocolBufferException) {
            logger.error("Did not process message in ERROR topic, cannot unserialize", e)
        }
    }

    /**
     * Generic serialization for Protobuf entities.
     *
     * @param entity current protobuf stub
     * @return bimnary payload
     */
    private fun <T> serializePayload(entity: T): ByteArray {
        require(entity is AbstractMessageLite<*, *>) {
            ("Protobuf entity is expected here for last parameter."
                    + "It should inherit from " + AbstractMessageLite::class.java + " but was " + entity!!::class.simpleName)
        }

        // Evaluate as a Protobuf Object
        val payload = ByteArrayOutputStream()
        val eventProtobuf = entity as AbstractMessageLite<*, *>
        return try {
            // Serialization (Binary)
            eventProtobuf.writeTo(payload)
            // Create
            payload.toByteArray()
        } catch (e: IOException) {
            throw IllegalStateException("Cannot create Kafka message payload s", e)
        }
    }
}
