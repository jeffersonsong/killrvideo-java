package com.killrvideo.service.statistic.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface VideoPlaybackStatsMapper {
    @DaoFactory
    VideoPlaybackStatsDao getVideoPlaybackStatsDao();

    static MapperBuilder<VideoPlaybackStatsMapper> build(CqlSession session) {
        return new VideoPlaybackStatsMapperBuilder(session);
    }
}
