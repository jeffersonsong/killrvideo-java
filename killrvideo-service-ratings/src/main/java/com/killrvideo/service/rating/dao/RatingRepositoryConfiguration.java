package com.killrvideo.service.rating.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RatingRepositoryConfiguration {
    @Bean
    public VideoRatingDao getVideoRatingDao(CqlSession session) {
        return new VideoRatingMapperBuilder(session).build().getVideoRatingDao();
    }

    @Bean
    public VideoRatingByUserDao getVideoRatingByUserDao(CqlSession session) {
        return new VideoRatingByUserMapperBuilder(session).build().getVideoRatingByUserDao();
    }
}
