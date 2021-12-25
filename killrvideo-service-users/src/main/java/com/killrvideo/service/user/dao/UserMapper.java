package com.killrvideo.service.user.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface UserMapper {
    @DaoFactory
    UserDao getUserDao();

    @DaoFactory
    UserCredentialsDao getUserCredentialsDao();

    static MapperBuilder<UserMapper> builder(CqlSession session) {
        return new UserMapperBuilder(session);
    }
}
