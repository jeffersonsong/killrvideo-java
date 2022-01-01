package com.killrvideo.service.user.dao

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory
import com.datastax.oss.driver.api.mapper.annotations.Mapper

@Mapper
interface UserMapper {
    @get:DaoFactory
    val userDao: UserDao

    @get:DaoFactory
    val userCredentialsDao: UserCredentialsDao
}
