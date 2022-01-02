package com.killrvideo.service.rating.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoRatingDaoConfig {
    @Bean
    public VideoRatingMapper videoRatingMapper(CqlSession session) {
        return VideoRatingMapper.build(session).build();
    }
}
