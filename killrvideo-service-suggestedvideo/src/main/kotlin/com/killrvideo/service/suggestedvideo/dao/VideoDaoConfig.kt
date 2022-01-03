package com.killrvideo.service.suggestedvideo.dao

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VideoDaoConfig {
    @Bean
    open fun videoMapper(session: CqlSession?): VideoMapper? {
        return VideoMapperBuilder(session).build()
    }
}