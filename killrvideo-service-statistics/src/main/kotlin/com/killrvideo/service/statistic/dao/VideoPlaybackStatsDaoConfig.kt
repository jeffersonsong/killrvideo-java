package com.killrvideo.service.statistic.dao

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VideoPlaybackStatsDaoConfig {
    @Bean
    open fun videoPlaybackStatsMapper(session: CqlSession?): VideoPlaybackStatsMapper? {
        return VideoPlaybackStatsMapperBuilder(session).build()
    }
}
