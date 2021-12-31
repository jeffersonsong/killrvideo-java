package com.killrvideo.utils.test

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable
import com.datastax.oss.driver.api.core.cql.Row
import java.lang.SafeVarargs
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.dse.utils.PageableQueryFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import java.util.function.Function

// TODO - Remove JvmStatic, convert to mockk
object CassandraTestUtils {
    @JvmStatic
    fun <T> mockMappedAsyncPagingIterable(list: List<T>?): MappedAsyncPagingIterable<T> {
        val iter = mock(MappedAsyncPagingIterable::class.java) as MappedAsyncPagingIterable<T>
        `when`(iter.hasMorePages()).thenReturn(false)
        `when`(iter.currentPage()).thenReturn(list)
        return iter
    }

    @JvmStatic
    @SafeVarargs
    fun <T> mockPageableQueryFactory(vararg queries: PageableQuery<T>?): PageableQueryFactory {
        val pageableQueryFactory = mock(PageableQueryFactory::class.java)
        when (queries.size) {
            0 -> {}
            1 -> `when`(
                pageableQueryFactory.newPageableQuery(any(), any(), any<Any>() as Function<Row?, T>?)
            ).thenReturn(queries[0])
            else -> `when`(
                pageableQueryFactory.newPageableQuery(any(), any(), any<Any>() as Function<Row?, T>?)
            ).thenReturn(queries[0], *Arrays.copyOfRange(queries, 1, queries.size))
        }
        return pageableQueryFactory
    }
}
