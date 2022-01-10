package com.killrvideo.service.video.dao

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VideoCatalogDaoConfig {
    @Bean
    open fun videoCatalogMapper(session: CqlSession): VideoCatalogMapper {
        return VideoCatalogMapperBuilder(session).build()
    }
}
