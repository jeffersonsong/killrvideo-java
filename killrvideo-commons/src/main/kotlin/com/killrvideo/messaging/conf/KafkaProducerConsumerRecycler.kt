package com.killrvideo.messaging.conf

import com.killrvideo.conf.KillrVideoConfiguration
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Component
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_KAFKA)
class KafkaProducerConsumerRecycler(
    @Qualifier("kafka.producer") private val producer: KafkaProducer<String, ByteArray>,
    @Qualifier("kafka.consumer.userCreating") private val consumerUserCreatedProtobuf: KafkaConsumer<String, ByteArray>,
    @Qualifier("kafka.consumer.videoRating") private val consumerVideoRatingProtobuf: KafkaConsumer<String, ByteArray>,
    @Qualifier("kafka.consumer.videoCreating") private val consumerVideoCreatedProtobuf: KafkaConsumer<String, ByteArray>,
    @Qualifier("kafka.consumer.error") private val errorConsumer: KafkaConsumer<String, ByteArray>
) {
    private val logger = KotlinLogging.logger { }

    @PreDestroy
    fun onDestroy() {
        logger.info { "Close producer" }
        producer.close()

        logger.info { "Close consumer kafka.consumer.userCreating" }
        consumerUserCreatedProtobuf.close()

        logger.info { "Close consumer kafka.consumer.videoCreating" }
        consumerVideoCreatedProtobuf.close()

        logger.info { "Close consumer kafka.consumer.videoRating" }
        consumerVideoRatingProtobuf.close()

        logger.info { "Close consumer kafka.consumer.error" }
        errorConsumer.close()
    }
}
