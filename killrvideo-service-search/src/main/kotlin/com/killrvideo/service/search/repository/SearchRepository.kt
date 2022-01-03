package com.killrvideo.service.search.repository

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.api.core.cql.BoundStatement
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.dse.utils.PageableQueryFactory
import com.killrvideo.service.search.dao.VideoRowMapper
import com.killrvideo.service.search.dto.Video
import com.killrvideo.service.search.repository.SearchRepository
import com.killrvideo.service.search.request.GetQuerySuggestionsRequestData
import com.killrvideo.service.search.request.SearchVideosRequestData
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
class SearchRepository(
    private val session: CqlSession, pageableQueryFactory: PageableQueryFactory,
    @Qualifier("searchVideoRowMapper") videoRowMapper: VideoRowMapper
) {
    /**
     * Precompile statements to speed up queries.
     */
    private val findSuggestedTags: PreparedStatement
    private val findVideosByTags: PageableQuery<Video>

    /**
     * Create a set of sentence conjunctions and other "undesirable"
     * words we will use later to exclude from search results.
     * Had to use .split() below because of the following conversation:
     * https://github.com/spring-projects/spring-boot/issues/501
     */
    @Value("#{'\${killrvideo.search.ignoredWords}'.split(',')}")
    private var ignoredWords: Set<String> = HashSet()

    init {
        // Statement for tags
        findSuggestedTags = session.prepare(QUERY_SUGGESTED_TAGS)

        // Statement for videos
        findVideosByTags = pageableQueryFactory.newPageableQuery(
            QUERY_VIDEO_BY_TAGS,
            ConsistencyLevel.LOCAL_ONE
        ) { row: Row -> videoRowMapper.map(row) }
    }

    /**
     * Do a Solr query against DSE search to find videos using Solr's ExtendedDisMax query parser. Query the
     * name, tags, and description fields in the videos table giving a boost to matches in the name and tags
     * fields as opposed to the description field
     * More info on ExtendedDisMax: http://wiki.apache.org/solr/ExtendedDisMax
     *
     *
     * Notice the "paging":"driver" parameter.  This is to ensure we dynamically
     * enable pagination regardless of our nodes dse.yaml setting.
     * https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/cursorsDeepPaging.html#cursorsDeepPaging__srchCursorCQL
     */
    suspend fun searchVideosAsync(request: SearchVideosRequestData): ResultListPage<Video> =
        findVideosByTags.queryNext(
            request.pageSize,
            request.pagingState,
            buildSolrQueryToSearchVideos(request.query)
        ).await()

    /**
     * In this case we are using DSE Search to query across the name, tags, and
     * description columns with a boost on name and tags.  Note that tags is a
     * collection of tags per each row with no extra steps to include all data
     * in the collection.
     *
     *
     * This is a more comprehensive search as
     * we are not just looking at values within the tags column, but also looking
     * across the other fields for similar occurrences.  This is especially helpful
     * if there are no tags for a given video as it is more likely to give us results.
     */
    private fun buildSolrQueryToSearchVideos(query: String): String {
        LOGGER.debug("Start searching videos by name, tag, and description")
        // Escaping special characters for query
        val replaceFind = " "
        val replaceWith = " AND "
        /*
         * Perform a query using DSE search to find videos. Query the
         * name, tags, and description columns in the videos table giving a boost to matches in the name and tags
         * columns as opposed to the description column.
         */
        val requestQuery = query.trim { it <= ' ' }
            .replace(replaceFind.toRegex(), Matcher.quoteReplacement(replaceWith))

        /*
         * In this case we are using DSE Search to query across the name, tags, and
         * description columns with a boost on name and tags.  The boost will put
         * more priority on the name column, then tags, and finally description.
         *
         * Note that tags is a
         * collection of tags per each row with no extra steps to include all data
         * in the collection.  This is a more comprehensive search as
         * we are not just looking at values within the tags column, but also looking
         * across the other columns for similar occurrences.  This is especially helpful
         * if there are no tags for a given video as it is more likely to give us results.
         *
         * Refer to the following documentation for a deeper look at term boosting:
         * https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/advancedTerms.html
         */return StringBuilder()
            .append(PAGING_DRIVER_START)
            .append("name:(").append(requestQuery).append("*)^4 OR ")
            .append("tags:(").append(requestQuery).append("*)^2 OR ")
            .append("description:(").append(requestQuery).append("*)")
            .append(PAGING_DRIVER_END)
            .toString()
    }

    /**
     * Search for tags starting with provided query string (ASYNC).
     *
     * @param request     request.
     * @return tags.
     */
    suspend fun getQuerySuggestionsAsync(request: GetQuerySuggestionsRequestData): Set<String> {
        val stmt = createStatementToQuerySuggestions(request.query, request.pageSize)
        return session.executeAsync(stmt).toCompletableFuture()
            .thenApply { rs: AsyncResultSet -> mapTagSet(rs, request.query) }.await()
    }

    /**
     * Do a query against DSE search to find query suggestions using a simple search.
     * The search_suggestions "column" references a field we created in our search index
     * to store name and tag data.
     *
     *
     * Notice the "paging":"driver" parameter.  This is to ensure we dynamically
     * enable pagination regardless of our nodes dse.yaml setting.
     * https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/cursorsDeepPaging.html#cursorsDeepPaging__srchCursorCQL
     */
    private fun createStatementToQuerySuggestions(query: String, fetchSize: Int): BoundStatement {
        val solrQuery = StringBuilder()
            .append(PAGING_DRIVER_START)
            .append("name:(").append(query).append("*) OR ")
            .append("tags:(").append(query).append("*) OR ")
            .append("description:(").append(query).append("*)")
            .append(PAGING_DRIVER_END)
        LOGGER.debug("getQuerySuggestions() solr_query is : {}", solrQuery)
        val stmt = findSuggestedTags.boundStatementBuilder(solrQuery.toString())
            .setPageSize(fetchSize)
            .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
            .build()
        LOGGER.debug("getQuerySuggestions: {} with solr_query: {}", stmt.preparedStatement.query, solrQuery)
        return stmt
    }

    /**
     * Here, we are inserting the request from the search bar, maybe something
     * like "c", "ca", or "cas" as someone starts to type the word "cassandra".
     *
     *
     * For each of these cases we are looking for any words in the search data that
     * start with the values above.
     *
     * @param rs           current resultset
     * @param requestQuery query
     * @return set of tags
     */
    private fun mapTagSet(rs: AsyncResultSet, requestQuery: String): TreeSet<String> {
        val checkRegex = Pattern.compile("(?i)\\b$requestQuery[a-z]*\\b")
        val suggestionSet = TreeSet<String>()
        for (row in rs.currentPage()) {
            /*
             * Since I simply want matches from both the name and tags fields
             * concatenate them together, apply regex, and add any results into
             * our suggestionSet TreeSet.  The TreeSet will handle any duplicates.
             */
            val name = row.getString(Video.COLUMN_NAME)
            val tags = row.getSet(Video.COLUMN_TAGS, String::class.java)
            val regexMatcher = checkRegex.matcher(name + tags.toString())
            while (regexMatcher.find()) {
                suggestionSet.add(regexMatcher.group().lowercase(Locale.getDefault()))
            }
            suggestionSet.removeAll(ignoredWords)
        }
        LOGGER.debug("TagSet returned are {}", suggestionSet)
        return suggestionSet
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SearchRepository::class.java)
        private const val QUERY_SUGGESTED_TAGS = "SELECT name, tags, description " +
                "FROM killrvideo.videos " +
                "WHERE solr_query = ?"
        private const val QUERY_VIDEO_BY_TAGS = "SELECT * " +
                "FROM killrvideo.videos " +
                "WHERE solr_query = ?"

        /**
         * Wrap search queries with "paging":"driver" to dynamically enable
         * paging to ensure we pull back all available results in the application.
         * https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/cursorsDeepPaging.html#cursorsDeepPaging__using-paging-with-cql-solr-queries-solrquery-Rim2GsbY
         */
        private const val PAGING_DRIVER_START = "{\"q\":\""
        private const val PAGING_DRIVER_END = "\", \"paging\":\"driver\"}"
    }
}
