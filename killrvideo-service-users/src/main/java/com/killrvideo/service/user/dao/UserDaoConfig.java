package com.killrvideo.service.user.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDaoConfig {
    @Bean
    public UserMapper userMapper(CqlSession session) {
        return UserMapper.builder(session).build();
    }
}
