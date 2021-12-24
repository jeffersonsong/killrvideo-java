package com.killrvideo.service.user.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRepositoryConfiguration {
    @Bean
    public UserDao getUserDao(CqlSession session) {
        return new UserMapperBuilder(session).build().getUserDao();
    }

    @Bean
    public UserCredentialsDao getUserCredentialsDao(CqlSession session) {
        return new UserCredentialsMapperBuilder(session).build().getUserCredentialsDao();
    }
}
