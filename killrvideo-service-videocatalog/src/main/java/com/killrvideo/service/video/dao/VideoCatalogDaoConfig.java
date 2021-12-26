package com.killrvideo.service.video.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoCatalogDaoConfig {
    @Bean
    public VideoCatalogMapper videoCatalogMapper(CqlSession session) {
        return VideoCatalogMapper.build(session).build();
    }
}
