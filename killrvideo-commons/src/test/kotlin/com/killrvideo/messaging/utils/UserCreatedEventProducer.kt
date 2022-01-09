package com.killrvideo.messaging.utils

import com.google.protobuf.Timestamp
import com.killrvideo.messaging.utils.KafkaProducerExtensions.sendAsync
import com.killrvideo.utils.FormatUtils
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import killrvideo.user_management.events.UserManagementEvents.UserCreated
import killrvideo.user_management.events.userCreated
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.concurrent.CompletableFuture
import java.lang.InterruptedException
import java.util.*
import java.util.concurrent.ExecutionException

object UserCreatedEventProducer {
    private const val TOPIC = "topic-kv-userCreation"

    @JvmStatic
    fun main(args: Array<String>) {
        //create kafka producer
        val producer = producer()

        Runtime.getRuntime().addShutdownHook(Thread {
            producer.close()
        })

        //prepare the record
        //String recordValue = "Current time is " + Instant.now().toString();
        val event = sampleEvent()
        println("Sending message to $TOPIC: ${FormatUtils.format(event)}")
        val record = ProducerRecord<String, ByteArray>(TOPIC, event.toByteArray())
        val completableFuture: CompletableFuture<RecordMetadata> = producer.sendAsync(record)

        try {
            completableFuture.get()
            println("Message sent to $TOPIC.")
        } catch (ex: ExecutionException) {
            ex.printStackTrace()
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        } finally {
            //close the producer at the end
            producer.close()
        }
    }

    private fun producer(): KafkaProducer<String, ByteArray> {
        val properties = Properties()
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java
        return KafkaProducer(properties)
    }

    private fun sampleEvent(): UserCreated =
        userCreated {
            userId = randomUuid()
            firstName = "Joe"
            lastName = "Smith"
            email = "joe.smith@gmail.com"
            timestamp = Timestamp.getDefaultInstance()
        }
}
