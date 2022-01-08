package com.killrvideo.redis.conf

import mu.KotlinLogging
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy
import javax.inject.Inject

@Component
class RedisClientRecycler {
    private val logger = KotlinLogging.logger {  }

    @Inject
    private lateinit var redisson: RedissonClient

    @PreDestroy
    fun onDestry() {
        logger.info { "Shutting down redis client..." }
        redisson.shutdown()
        logger.info { "Shut down redis client." }
    }
}
