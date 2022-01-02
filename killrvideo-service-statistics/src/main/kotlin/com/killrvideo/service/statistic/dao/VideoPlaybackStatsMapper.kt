package com.killrvideo.service.statistic.dao

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory
import com.datastax.oss.driver.api.mapper.annotations.Mapper

@Mapper
interface VideoPlaybackStatsMapper {
    @get:DaoFactory
    val videoPlaybackStatsDao: VideoPlaybackStatsDao
}
