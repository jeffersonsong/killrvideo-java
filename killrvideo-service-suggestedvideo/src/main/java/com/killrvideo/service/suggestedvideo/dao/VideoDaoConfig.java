package com.killrvideo.service.suggestedvideo.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoDaoConfig {
    @Bean
    public VideoMapper videoMapper(CqlSession session) {
        return VideoMapper.build(session).build();
    }
}
