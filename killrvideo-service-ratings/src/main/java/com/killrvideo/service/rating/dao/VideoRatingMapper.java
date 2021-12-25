package com.killrvideo.service.rating.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface VideoRatingMapper {
    @DaoFactory
    VideoRatingDao getVideoRatingDao();

    @DaoFactory
    VideoRatingByUserDao getVideoRatingByUserDao();

    static MapperBuilder<VideoRatingMapper> build(CqlSession session) {
        return new VideoRatingMapperBuilder(session);
    }
}
