package com.killrvideo.service.rating.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface VideoRatingByUserMapper {
    @DaoFactory
    VideoRatingByUserDao getVideoRatingByUserDao();
}
