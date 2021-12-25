package com.killrvideo.service.search.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.killrvideo.dse.dto.AbstractVideo.COLUMN_NAME;
import static com.killrvideo.dse.dto.Video.*;

@Repository
public class SearchRepository {
    /** Logger for that class. */
    private static Logger LOGGER = LoggerFactory.getLogger(SearchRepository.class);

    /** Precompile statements to speed up queries. */
    private PreparedStatement findSuggestedTags;

    private PreparedStatement findVideosByTags;

    /**
     * Create a set of sentence conjunctions and other "undesirable"
     * words we will use later to exclude from search results.
     * Had to use .split() below because of the following conversation:
     * https://github.com/spring-projects/spring-boot/issues/501
     */
    @Value("#{'${killrvideo.search.ignoredWords}'.split(',')}")
    private Set<String> ignoredWords = new HashSet<>();

    /**
     * Wrap search queries with "paging":"driver" to dynamically enable
     * paging to ensure we pull back all available results in the application.
     * https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/cursorsDeepPaging.html#cursorsDeepPaging__using-paging-with-cql-solr-queries-solrquery-Rim2GsbY
     */
    final private String pagingDriverStart = "{\"q\":\"";
    final private String pagingDriverEnd = "\", \"paging\":\"driver\"}";

    private CqlSession session;

    public SearchRepository(CqlSession session) {
        this.session = session;

        // Statement for tags
        this.findSuggestedTags = session.prepare(
                "SELECT name, tags, description " +
                        "FROM killrvideo.videos " +
                        "WHERE solr_query = ?"
        );

        // Statement for videos
        this.findVideosByTags = session.prepare(
                "SELECT * " +
                        "FROM killrvideo.videos " +
                        "WHERE solr_query = ?"
        );
    }

    /**
     * Do a Solr query against DSE search to find videos using Solr's ExtendedDisMax query parser. Query the
     * name, tags, and description fields in the videos table giving a boost to matches in the name and tags
     * fields as opposed to the description field
     * More info on ExtendedDisMax: http://wiki.apache.org/solr/ExtendedDisMax
     *
     * Notice the "paging":"driver" parameter.  This is to ensure we dynamically
     * enable pagination regardless of our nodes dse.yaml setting.
     * https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/cursorsDeepPaging.html#cursorsDeepPaging__srchCursorCQL
     */
    public CompletableFuture<ResultListPage<Video>> searchVideosAsync(String query, int fetchSize, Optional<String> pagingState) {
        return session.executeAsync(createStatementToSearchVideos(query, fetchSize, pagingState))
                .toCompletableFuture()
                .thenApply(rs -> new ResultListPage<Video>(rs, this::mapToVideo));
    }

    /**
     * In this case we are using DSE Search to query across the name, tags, and
     * description columns with a boost on name and tags.  Note that tags is a
     * collection of tags per each row with no extra steps to include all data
     * in the collection.
     *
     * This is a more comprehensive search as
     * we are not just looking at values within the tags column, but also looking
     * across the other fields for similar occurrences.  This is especially helpful
     * if there are no tags for a given video as it is more likely to give us results.
     */
    private BoundStatement createStatementToSearchVideos(String query, int fetchSize, Optional<String> pagingState) {
        LOGGER.debug("Start searching videos by name, tag, and description");
        // Escaping special characters for query
        final String replaceFind = " ";
        final String replaceWith = " AND ";
        /**
         * Perform a query using DSE search to find videos. Query the
         * name, tags, and description columns in the videos table giving a boost to matches in the name and tags
         * columns as opposed to the description column.
         */
        String requestQuery = query.trim()
                .replaceAll(replaceFind, Matcher.quoteReplacement(replaceWith));

        /**
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
        final StringBuilder solrQuery = new StringBuilder()
                .append(pagingDriverStart)
                .append("name:(").append(requestQuery).append("*)^4 OR ")
                .append("tags:(").append(requestQuery).append("*)^2 OR ")
                .append("description:(").append(requestQuery).append("*)")
                .append(pagingDriverEnd);

        BoundStatementBuilder builder = findVideosByTags.boundStatementBuilder(solrQuery.toString());
        pagingState.ifPresent( x -> builder.setPagingState(Bytes.fromHexString(x)));
        builder.setPageSize(fetchSize);
        builder.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

        BoundStatement stmt = builder.build();
        LOGGER.debug("Executed query is {} with solr_query: {}", stmt.getPreparedStatement().getQuery(),solrQuery);
        return stmt;
    }

    private Video mapToVideo(Row row) {
        return new Video(
                row.getUuid(COLUMN_VIDEOID),
                row.getUuid(COLUMN_USERID),
                row.getString(COLUMN_NAME),
                row.getString(COLUMN_DESCRIPTION),
                row.getString(COLUMN_LOCATION),
                row.getInt(COLUMN_LOCATIONTYPE),
                row.getString(COLUMN_PREVIEW),
                row.getSet(COLUMN_TAGS, String.class),
                row.getInstant(COLUMN_ADDED_DATE)
        );
    }

    /**
     * Search for tags starting with provided query string (ASYNC).
     *
     * @param query
     * 		pattern
     * @param fetchSize
     * 		numbner of results to retrieve
     * @return
     */
    public CompletableFuture <TreeSet< String >> getQuerySuggestionsAsync(String query, int fetchSize) {
        BoundStatement stmt = createStatementToQuerySuggestions(query, fetchSize);
        return this.session.executeAsync(stmt).toCompletableFuture()
                .thenApply(rs -> mapTagSet(rs, query));
    }

    /**
     * Do a query against DSE search to find query suggestions using a simple search.
     * The search_suggestions "column" references a field we created in our search index
     * to store name and tag data.
     *
     * Notice the "paging":"driver" parameter.  This is to ensure we dynamically
     * enable pagination regardless of our nodes dse.yaml setting.
     * https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/cursorsDeepPaging.html#cursorsDeepPaging__srchCursorCQL
     */
    private BoundStatement createStatementToQuerySuggestions(String query, int fetchSize) {
        final StringBuilder solrQuery = new StringBuilder()
                .append(pagingDriverStart)
                .append("name:(").append(query).append("*) OR ")
                .append("tags:(").append(query).append("*) OR ")
                .append("description:(").append(query).append("*)")
                .append(pagingDriverEnd);
        LOGGER.debug("getQuerySuggestions() solr_query is : {}", solrQuery.toString());

        BoundStatementBuilder builder = findSuggestedTags.boundStatementBuilder(solrQuery.toString());
        builder.setPageSize(fetchSize);
        builder.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        BoundStatement stmt = builder.build();
        LOGGER.debug("getQuerySuggestions: {} with solr_query: {}", stmt.getPreparedStatement().getQuery(), solrQuery);
        return stmt;
    }

    /**
     * Here, we are inserting the request from the search bar, maybe something
     * like "c", "ca", or "cas" as someone starts to type the word "cassandra".
     *
     * For each of these cases we are looking for any words in the search data that
     * start with the values above.
     *
     * @param rs
     * 		current resultset
     * @param requestQuery
     * 		query
     * @return
     * 		set of tags
     */
    private TreeSet<String> mapTagSet(AsyncResultSet rs, String requestQuery) {
        final Pattern checkRegex = Pattern.compile("(?i)\\b" + requestQuery + "[a-z]*\\b");
        TreeSet< String > suggestionSet = new TreeSet<>();
        for (Row row : rs.currentPage()) {
            /**
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
