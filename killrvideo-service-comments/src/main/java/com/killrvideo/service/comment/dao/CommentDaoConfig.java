package com.killrvideo.service.comment.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommentDaoConfig {
    @Bean
    public CommentMapper commentMapper(CqlSession session) {
        return CommentMapper.build(session).build();
    }
}
