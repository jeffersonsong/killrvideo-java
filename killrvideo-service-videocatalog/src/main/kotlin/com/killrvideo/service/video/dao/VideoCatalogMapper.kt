package com.killrvideo.service.video.dao

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory
import com.datastax.oss.driver.api.mapper.annotations.Mapper

@Mapper
interface VideoCatalogMapper {
    @get:DaoFactory
    val videoDao: VideoDao

    @get:DaoFactory
    val latestVideoDao: LatestVideoDao

    @get:DaoFactory
    val userVideoDao: UserVideoDao
}
