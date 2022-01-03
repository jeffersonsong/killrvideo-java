package com.killrvideo.dse.utils

import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.protocol.internal.util.Bytes
import com.killrvideo.dse.dto.ResultListPage
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.StreamSupport

object AsyncResultSetUtils {
    fun <T> toResultListPage(rs: AsyncResultSet, mapper: Function<Row, T>): ResultListPage<T> {
        val list = getResultsOnCurrentPage(rs, mapper)
        val nextPage = getPagingState(rs)
        return ResultListPage(list, nextPage)
    }

    private fun <T> getResultsOnCurrentPage(rs: AsyncResultSet, mapper: Function<Row, T>): List<T> {
        return StreamSupport.stream(rs.currentPage().spliterator(), false)
            .map(mapper).collect(Collectors.toList())
    }

    fun getPagingState(rs: AsyncResultSet): String? {
        return if (rs.hasMorePages() && rs.executionInfo.pagingState != null) {
            Bytes.toHexString(rs.executionInfo.pagingState)
        } else {
            null
        }
    }
}
