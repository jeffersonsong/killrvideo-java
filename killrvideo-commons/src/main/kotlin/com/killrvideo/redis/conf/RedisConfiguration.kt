package com.killrvideo.redis.conf

import com.killrvideo.discovery.ServiceDiscoveryDao
import mu.KotlinLogging
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RedisConfiguration(
    private val discoveryDao: ServiceDiscoveryDao,
    @Value("\${killrvideo.discovery.service.redis: redis}")
    private val redisServiceName: String
) {
    private val logger = KotlinLogging.logger { }
    @Bean
    open fun initializeRedissonClient(): RedissonClient {
        val endPointList = discoveryDao.lookup(redisServiceName)
        if (endPointList.isEmpty()) {
            throw IllegalStateException("Redis not configured. You can setup env var KILLRVIDEO_REDIS_CONTACT_POINTS")
        }
        val redisUrl = "redis://${endPointList[0]}"
        logger.info { "Connecting to redis server: $redisUrl..." }
        val config = Config()
        config.useSingleServer().address = redisUrl
        return Redisson.create(config)
    }
}
