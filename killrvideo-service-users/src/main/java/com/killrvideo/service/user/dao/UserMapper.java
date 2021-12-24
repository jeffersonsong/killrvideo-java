package com.killrvideo.service.user.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface UserMapper {
    @DaoFactory
    UserDao getUserDao();
}
