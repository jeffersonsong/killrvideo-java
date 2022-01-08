package com.killrvideo.redis.conf

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RedisConfiguration {
    @Bean
    open fun initializeRedissonClient(): RedissonClient {
        return Redisson.create()
    }
}
