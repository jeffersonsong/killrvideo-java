package com.killrvideo.messaging.dao

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.JdkFutureAdapters
import com.google.protobuf.AbstractMessageLite
import com.google.protobuf.InvalidProtocolBufferException
import killrvideo.common.CommonEvents.ErrorEvent
import java.util.concurrent.CompletableFuture
import com.killrvideo.conf.KillrVideoConfiguration
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.consumer.KafkaConsumer
import mu.KotlinLogging
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.concurrent.Executors
import java.util.stream.StreamSupport
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.IllegalStateException
import java.time.Duration
import java.util.concurrent.Executor
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
    protected lateinit var protobufProducer: KafkaProducer<String, ByteArray>

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
    @Value("\${killrvideo.messaging.topics.errors: topic-kv-errors}")
    override lateinit var errorDestination: String

    /**
     * {@inheritDoc}
     */
    override fun sendEvent(targetDestination: String, event: Any): CompletableFuture<Any> {
        logger.info("Sending Event '{}' ..", event.javaClass.name)
        val payload = serializePayload<Any?>(event)
        logger.info("Sending Event '{}' ..", event.javaClass.name)
        val cfv = CompletableFuture<Any>()
        val myCallback: FutureCallback<RecordMetadata> = object : FutureCallback<RecordMetadata> {
            override fun onFailure(ex: Throwable) {
                cfv.completeExceptionally(ex)
            }

            override fun onSuccess(rs: RecordMetadata) {
                cfv.complete(rs)
            }
        }
        val listenable = JdkFutureAdapters.listenInPoolThread(
            protobufProducer.send(
                ProducerRecord(targetDestination, payload)
            )
        )
        val executor: Executor = Executors.newFixedThreadPool(2)
        Futures.addCallback(listenable, myCallback, executor)
        return cfv
    }

    // -- Common Error Handling --
    @PostConstruct
    fun registerErrorConsumer() {
        logger.info("Start consuming events from topic '{}' ..", errorDestination)
        errorLogger.subscribe(listOf(errorDestination))
        StreamSupport.stream(errorLogger.poll(Duration.ofSeconds(5)).spliterator(), false)
            .map { obj: ConsumerRecord<String, ByteArray> -> obj.value() }
            .forEach { eventErrorPayload: ByteArray -> consumeErrorEvent(eventErrorPayload) }
    }

    /**
     * Reading topic Error so expecting
     *
     * @param eventErrorPayload
     */
    fun consumeErrorEvent(eventErrorPayload: ByteArray) {
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
