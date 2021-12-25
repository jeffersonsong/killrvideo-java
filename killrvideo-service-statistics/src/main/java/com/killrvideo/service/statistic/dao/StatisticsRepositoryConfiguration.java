package com.killrvideo.service.statistic.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatisticsRepositoryConfiguration {
    @Bean
    public VideoPlaybackStatsDao getVideoPlaybackStatsDao(CqlSession session) {
        return new VideoPlaybackStatsMapperBuilder(session).build().getVideoPlaybackStatsDao();
    }
}
