package com.killrvideo.service.user.dao

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UserDaoConfig {

    @Bean
    open fun userMapper(session: CqlSession): UserMapper {
        return UserMapperBuilder(session).build()
    }
}
