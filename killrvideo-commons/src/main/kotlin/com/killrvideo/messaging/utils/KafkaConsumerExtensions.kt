package com.killrvideo.messaging.utils

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.function.Consumer

object KafkaConsumerExtensions {
    val logger = KotlinLogging.logger { }

    fun <K, V> KafkaConsumer<K, V>.receiveMessages(
        topic: String,
        timeout: Duration = Duration.ofMillis(500),
        processor: Consumer<ConsumerRecord<K, V>>
    ) {
        Thread({
            logger.info { "Start consuming events from topic '$topic' .." }
            subscribe(listOf(topic))

            while (true) {
                val records = poll(timeout)
                records.forEach { processor.accept(it) }
                commitAsync()
            }
        }, "consumer-$topic").start()
    }
}
