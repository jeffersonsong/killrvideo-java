package com.killrvideo.service.comment.dao

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class CommentDaoConfig {
    @Bean
    open fun commentMapper(session: CqlSession?): CommentMapper? {
        return CommentMapperBuilder(session).build()
    }
}
