package com.killrvideo.service.comment.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface CommentMapper {
    @DaoFactory
    CommentByUserDao getCommentByUserDao();

    @DaoFactory
    CommentByVideoDao getCommentByVideoDao();

    static MapperBuilder<CommentMapper> build(CqlSession session) {
        return new CommentMapperBuilder(session);
    }
}
