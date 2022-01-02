package com.killrvideo.service.statistic.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoPlaybackStatsDaoConfig {
    @Bean
    public VideoPlaybackStatsMapper videoPlaybackStatsMapper(CqlSession session) {
        return VideoPlaybackStatsMapper.build(session).build();
    }
}
