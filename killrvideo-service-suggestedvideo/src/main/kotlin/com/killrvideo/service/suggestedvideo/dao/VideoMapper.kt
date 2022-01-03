package com.killrvideo.service.suggestedvideo.dao

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory
import com.datastax.oss.driver.api.mapper.annotations.Mapper

@Mapper
interface VideoMapper {
    @get:DaoFactory
    val videoDao: VideoDao
}
