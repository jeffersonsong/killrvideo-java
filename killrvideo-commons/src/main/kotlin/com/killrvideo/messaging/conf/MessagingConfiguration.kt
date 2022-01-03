package com.killrvideo.messaging.conf

import com.google.common.eventbus.EventBus
import com.killrvideo.conf.KillrVideoConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Store all configuration related to Messagging.
 *
 * @author DataStax Developer Advocates team.
 */
@Configuration
@Profile(KillrVideoConfiguration.PROFILE_MESSAGING_MEMORY)
open class MessagingConfiguration {
    /**
     * Getter for attribute 'minThreads'.
     *
     * @return
     * current value of 'minThreads'
     */
    @Value("\${killrvideo.messaging.inmemory.threadpool.min.threads:5}")
    val minThreads = 0

    /**
     * Getter for attribute 'maxThreads'.
     *
     * @return
     * current value of 'maxThreads'
     */
    @Value("\${killrvideo.messaging.inmemory.max.threads:10}")
    val maxThreads = 0

    /**
     * Getter for attribute 'threadsTTLSeconds'.
     *
     * @return
     * current value of 'threadsTTLSeconds'
     */
    @Value("\${killrvideo.messaging.inmemory.ttlThreads:60}")
    val threadsTTLSeconds = 0L

    /**
     * Getter for attribute 'threadPoolQueueSize'.
     *
     * @return
     * current value of 'threadPoolQueueSize'
     */
    @Value("\${killrvideo.messaging.inmemory.queueSize:1000}")
    val threadPoolQueueSize = 0
    @Bean
    open fun createEventBus(): EventBus {
        return EventBus(EVENT_BUS_KILLRVIODEO)
    }

    /**
     * Initialize the threadPool.
     *
     * @return
     * current executor for this
     */
    @Bean(destroyMethod = "shutdownNow")
    open fun threadPool(): ExecutorService {
        return ThreadPoolExecutor(
            minThreads, maxThreads,
            threadsTTLSeconds, TimeUnit.SECONDS,
            LinkedBlockingQueue(threadPoolQueueSize),
            KillrVideoThreadFactory()
        )
    }

    companion object {
        /** Event Bus.  */
        private const val EVENT_BUS_KILLRVIODEO = "killrvideo_event_bus"
    }
}
