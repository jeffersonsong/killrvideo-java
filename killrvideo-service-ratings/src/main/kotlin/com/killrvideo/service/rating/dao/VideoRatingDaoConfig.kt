package com.killrvideo.service.rating.dao

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VideoRatingDaoConfig {
    @Bean
    open fun videoRatingMapper(session: CqlSession): VideoRatingMapper =
        VideoRatingMapperBuilder(session).build()
}
