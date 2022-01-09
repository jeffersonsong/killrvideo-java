package com.killrvideo.messaging.utils

import mu.KotlinLogging
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.CompletableFuture

object KafkaProducerExtensions {
    val logger = KotlinLogging.logger { }

    fun <K, V> KafkaProducer<K, V>.sendAsync(record: ProducerRecord<K, V>): CompletableFuture<RecordMetadata> =
        CompletableFuture<RecordMetadata>().apply {
            send(record) { metadata, exception ->
                if (exception != null) {
                    logger.error("Event sent failed '{}' ..", record.topic(), exception!!)
                    completeExceptionally(exception)
                } else {
                    logger.info("Event sent successfully '{}' ..", record.topic())
                    complete(metadata)
                }
            }
            flush()
        }
}
