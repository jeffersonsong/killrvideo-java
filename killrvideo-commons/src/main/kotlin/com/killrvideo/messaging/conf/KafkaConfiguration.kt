package com.killrvideo.messaging.conf

import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.discovery.ServiceDiscoveryDao
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.*
import javax.inject.Inject

/**
 * Use Kafka to exchange messages between services.
 *
 * @author Cedrick LUNVEN (@clunven) *
 */
@Configuration
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_KAFKA)
open class KafkaConfiguration {
    /** Kafka Server to be used.  */
    private var kafkaServer: String? = null

    @Value("\${kafka.ack: 1 }")
    private val producerAck: String? = null

    @Value("\${kafka.consumerGroup: killrvideo }")
    private val consumerGroup: String? = null

    @Inject
    private val discoveryDao: ServiceDiscoveryDao? = null

    /**
     * Should we init connection with ETCD or direct.
     *
     * @return
     * target kafka adress
     */
    private val kafkaServerConnectionUrl: String?
        private get() {
            if (null == kafkaServer) {
                kafkaServer = java.lang.String.join(",", discoveryDao!!.lookup(SERVICE_KAFKA))
            }
            return kafkaServer
        }

    @Bean("kafka.producer")
    open fun jsonProducer(): KafkaProducer<String, ByteArray> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServerConnectionUrl
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] =
            StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
        props[ProducerConfig.ACKS_CONFIG] = producerAck
        return KafkaProducer(props)
    }

    @Bean("kafka.consumer.videoRating")
    open fun videoRatingConsumer(): KafkaConsumer<String, ByteArray> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServerConnectionUrl
        props[ConsumerConfig.GROUP_ID_CONFIG] = consumerGroup
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        return KafkaConsumer(props)
    }

    @Bean("kafka.consumer.userCreating")
    open fun userCreatingConsumer(): KafkaConsumer<String, ByteArray> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServerConnectionUrl
        props[ConsumerConfig.GROUP_ID_CONFIG] = consumerGroup
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        return KafkaConsumer(props)
    }

    @Bean("kafka.consumer.videoCreating")
    open fun videoCreatingConsumer(): KafkaConsumer<String, ByteArray> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServerConnectionUrl
        props[ConsumerConfig.GROUP_ID_CONFIG] = consumerGroup
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] =
            StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        return KafkaConsumer(props)
    }

    @Bean("kafka.consumer.error")
    open fun errorConsumer(): KafkaConsumer<String, ByteArray> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServerConnectionUrl
        props[ConsumerConfig.GROUP_ID_CONFIG] = consumerGroup
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        return KafkaConsumer(props)
    }

    companion object {
        /** Name of service in ETCD.  */
        const val SERVICE_KAFKA = "kafka"

        /** Default CQL listening port.  */
        const val DEFAULT_PORT = 8082
    }
}
