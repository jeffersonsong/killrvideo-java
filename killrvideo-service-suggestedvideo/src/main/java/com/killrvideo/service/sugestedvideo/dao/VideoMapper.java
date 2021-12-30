package com.killrvideo.service.sugestedvideo.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface VideoMapper {
    @DaoFactory
    VideoDao getVideoDao();

    static MapperBuilder<VideoMapper> build(CqlSession session) {
        return new VideoMapperBuilder(session);
    }
}
