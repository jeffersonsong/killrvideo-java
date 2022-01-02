package com.killrvideo.service.comment.dao

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory
import com.datastax.oss.driver.api.mapper.annotations.Mapper

@Mapper
interface CommentMapper {
    @get:DaoFactory
    val commentByUserDao: CommentByUserDao

    @get:DaoFactory
    val commentByVideoDao: CommentByVideoDao
}
