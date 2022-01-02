package com.killrvideo.service.search.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.killrvideo.service.search.dao.VideoRowMapper;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.service.search.dto.Video;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;
import com.killrvideo.service.search.request.GetQuerySuggestionsRequestData;
import com.killrvideo.service.search.request.SearchVideosRequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
public class SearchRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchRepository.class);
    private static final String QUERY_SUGGESTED_TAGS =
            "SELECT name, tags, description " +
            "FROM killrvideo.videos " +
            "WHERE solr_query = ?";
    private static final String QUERY_VIDEO_BY_TAGS = "SELECT * " +
            "FROM killrvideo.videos " +
            "WHERE solr_query = ?";

    /**
     * Wrap search queries with "paging":"driver" to dynamically enable
     * paging to ensure we pull back all available results in the application.
     * https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/cursorsDeepPaging.html#cursorsDeepPaging__using-paging-with-cql-solr-queries-solrquery-Rim2GsbY
     */
    private static final String PAGING_DRIVER_START = "{\"q\":\"";
    private static final String PAGING_DRIVER_END = "\", \"paging\":\"driver\"}";

    /**
     * Precompile statements to speed up queries.
     */
    private final PreparedStatement findSuggestedTags;

    private final PageableQuery<Video> findVideosByTags;

    /**
     * Create a set of sentence conjunctions and other "undesirable"
     * words we will use later to exclude from search results.
     * Had to use .split() below because of the following conversation:
     * https://github.com/spring-projects/spring-boot/issues/501
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Value("#{'${killrvideo.search.ignoredWords}'.split(',')}")
    private final Set<String> ignoredWords = new HashSet<>();

    private final CqlSession session;

    public SearchRepository(CqlSession session, PageableQueryFactory pageableQueryFactory,
                            @Qualifier("searchVideoRowMapper") VideoRowMapper videoRowMapper) {
        this.session = session;

        // Statement for tags
        this.findSuggestedTags = session.prepare(QUERY_SUGGESTED_TAGS);

        // Statement for videos
        this.findVideosByTags = pageableQueryFactory.newPageableQuery(
                QUERY_VIDEO_BY_TAGS,
                ConsistencyLevel.LOCAL_ONE,
                videoRowMapper::map
        );
    }

    /**
     * Do a Solr query against DSE search to find videos using Solr's ExtendedDisMax query parser. Query the
     * name, tags, and description fields in the videos table giving a boost to matches in the name and tags
     * fields as opposed to the description field
     * More info on ExtendedDisMax: http://wiki.apache.org/solr/ExtendedDisMax
     * <p>
     * Notice the "paging":"driver" parameter.  This is to ensure we dynamically
     * enable pagination regardless of our nodes dse.yaml setting.
     * https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/cursorsDeepPaging.html#cursorsDeepPaging__srchCursorCQL
     */
    public CompletableFuture<ResultListPage<Video>> searchVideosAsync(SearchVideosRequestData request) {
        return findVideosByTags.queryNext(
                Optional.of(request.getPageSize()),
                request.getPagingState(),
                buildSolrQueryToSearchVideos(request.getQuery())
        );
    }

    /**
     * In this case we are using DSE Search to query across the name, tags, and
     * description columns with a boost on name and tags.  Note that tags is a
     * collection of tags per each row with no extra steps to include all data
     * in the collection.
     * <p>
     * This is a more comprehensive search as
     * we are not just looking at values within the tags column, but also looking
     * across the other fields for similar occurrences.  This is especially helpful
     * if there are no tags for a given video as it is more likely to give us results.
     */
    private String buildSolrQueryToSearchVideos(String query) {
        LOGGER.debug("Start searching videos by name, tag, and description");
        // Escaping special characters for query
        final String replaceFind = " ";
        final String replaceWith = " AND ";
        /*
         * Perform a query using DSE search to find videos. Query the
         * name, tags, and description columns in the videos table giving a boost to matches in the name and tags
         * columns as opposed to the description column.
         */
        String requestQuery = query.trim()
                .replaceAll(replaceFind, Matcher.quoteReplacement(replaceWith));

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
         */
        return new StringBuilder()
                .append(PAGING_DRIVER_START)
                .append("name:(").append(requestQuery).append("*)^4 OR ")
                .append("tags:(").append(requestQuery).append("*)^2 OR ")
                .append("description:(").append(requestQuery).append("*)")
                .append(PAGING_DRIVER_END)
                .toString();
    }

    /**
     * Search for tags starting with provided query string (ASYNC).
     *
     * @param request     request.
     * @return tags.
     */
    public CompletableFuture<Set<String>> getQuerySuggestionsAsync(GetQuerySuggestionsRequestData request) {
        BoundStatement stmt = createStatementToQuerySuggestions(request.getQuery(), request.getPageSize());
        return this.session.executeAsync(stmt).toCompletableFuture()
                .thenApply(rs -> mapTagSet(rs, request.getQuery()));
    }

    /**
     * Do a query against DSE search to find query suggestions using a simple search.
     * The search_suggestions "column" references a field we created in our search index
     * to store name and tag data.
     * <p>
     * Notice the "paging":"driver" parameter.  This is to ensure we dynamically
     * enable pagination regardless of our nodes dse.yaml setting.
     * https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/cursorsDeepPaging.html#cursorsDeepPaging__srchCursorCQL
     */
    private BoundStatement createStatementToQuerySuggestions(String query, int fetchSize) {
        final StringBuilder solrQuery = new StringBuilder()
                .append(PAGING_DRIVER_START)
                .append("name:(").append(query).append("*) OR ")
                .append("tags:(").append(query).append("*) OR ")
                .append("description:(").append(query).append("*)")
                .append(PAGING_DRIVER_END);
        LOGGER.debug("getQuerySuggestions() solr_query is : {}", solrQuery);

        BoundStatement stmt = findSuggestedTags.boundStatementBuilder(solrQuery.toString())
                .setPageSize(fetchSize)
                .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
                .build();
        LOGGER.debug("getQuerySuggestions: {} with solr_query: {}", stmt.getPreparedStatement().getQuery(), solrQuery);
        return stmt;
    }

    /**
     * Here, we are inserting the request from the search bar, maybe something
     * like "c", "ca", or "cas" as someone starts to type the word "cassandra".
     * <p>
     * For each of these cases we are looking for any words in the search data that
     * start with the values above.
     *
     * @param rs           current resultset
     * @param requestQuery query
     * @return set of tags
     */
    private TreeSet<String> mapTagSet(AsyncResultSet rs, String requestQuery) {
        final Pattern checkRegex = Pattern.compile("(?i)\\b" + requestQuery + "[a-z]*\\b");
        TreeSet<String> suggestionSet = new TreeSet<>();
        for (Row row : rs.currentPage()) {
            /*
             * Since I simply want matches from both the name and tags fields
             * concatenate them together, apply regex, and add any results into
             * our suggestionSet TreeSet.  The TreeSet will handle any duplicates.
             */
            String name = row.getString(Video.COLUMN_NAME);
            Set<String> tags = row.getSet(Video.COLUMN_TAGS, String.class);
            Matcher regexMatcher = checkRegex.matcher(name.concat(tags.toString()));
            while (regexMatcher.find()) {
                suggestionSet.add(regexMatcher.group().toLowerCase());
            }
            suggestionSet.removeAll(ignoredWords);
        }
        LOGGER.debug("TagSet returned are {}", suggestionSet);
        return suggestionSet;
    }
}
