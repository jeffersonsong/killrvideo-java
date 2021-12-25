package com.killrvideo.service.video.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface VideoCatalogMapper {
    @DaoFactory
    VideoDao getVideoDao();

    @DaoFactory
    LatestVideoDao getLatestVideoDao();

    @DaoFactory
    UserVideoDao getUserVideoDao();

    static MapperBuilder<VideoCatalogMapper> build(CqlSession session) {
        return new VideoCatalogMapperBuilder(session);
    }
}
