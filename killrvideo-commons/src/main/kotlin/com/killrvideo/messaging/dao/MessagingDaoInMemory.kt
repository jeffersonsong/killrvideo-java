package com.killrvideo.messaging.dao

import com.google.common.eventbus.EventBus
import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.utils.FormatUtils
import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * Wrapping any kind of messages.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository("killrvideo.dao.messaging.memory")
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_MEMORY)
class MessagingDaoInMemory : MessagingDao {
    private val logger = KotlinLogging.logger {  }
    @Inject
    private lateinit var eventBus: EventBus

    /** {@inheritDoc}  */
    override fun sendEvent(targetDestination: String, event: Any): CompletableFuture<Any?> {
        logger.info {"Publishing event type ${event.javaClass.name} to destination ${targetDestination}, Payload: ${FormatUtils.format(event)}"}
        eventBus.post(event)
        return CompletableFuture.supplyAsync {
            eventBus.post(event)
            null
        }
    }

    override val errorDestination: String
        get() = "ERROR"
}
