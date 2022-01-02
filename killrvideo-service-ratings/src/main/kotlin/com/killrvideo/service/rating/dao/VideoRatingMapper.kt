package com.killrvideo.service.rating.dao

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory
import com.datastax.oss.driver.api.mapper.annotations.Mapper

@Mapper
interface VideoRatingMapper {
    @get:DaoFactory
    val videoRatingDao: VideoRatingDao

    @get:DaoFactory
    val videoRatingByUserDao: VideoRatingByUserDao
}
