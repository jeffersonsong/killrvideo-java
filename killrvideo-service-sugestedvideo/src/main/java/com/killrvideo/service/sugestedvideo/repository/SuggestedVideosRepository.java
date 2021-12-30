package com.killrvideo.service.sugestedvideo.repository;

import com.datastax.dse.driver.api.core.graph.AsyncGraphResultSet;
import com.datastax.dse.driver.api.core.graph.FluentGraphStatement;
import com.datastax.dse.driver.api.core.graph.GraphNode;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.google.common.collect.Sets;
import com.killrvideo.dse.dao.VideoRowMapper;
import com.killrvideo.dse.dto.ResultListPage;
import com.killrvideo.dse.dto.Video;
import com.killrvideo.dse.graph.KillrVideoTraversal;
import com.killrvideo.dse.graph.KillrVideoTraversalSource;
import com.killrvideo.dse.graph.__;
import com.killrvideo.dse.utils.PageableQuery;
import com.killrvideo.dse.utils.PageableQueryFactory;
import com.killrvideo.service.sugestedvideo.dao.VideoDao;
import com.killrvideo.service.sugestedvideo.dao.VideoMapper;
import com.killrvideo.service.sugestedvideo.request.GetRelatedVideosRequestData;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.killrvideo.dse.graph.KillrVideoTraversalConstants.VERTEX_USER;
import static com.killrvideo.dse.graph.KillrVideoTraversalConstants.VERTEX_VIDEO;

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
public class SuggestedVideosRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestedVideosRepository.class);
    private static final String QUERY_RELATED_VIDEOS =
            "SELECT * " +
            "FROM killrvideo.videos " +
            "WHERE solr_query = ?";
    private final CqlSession session;
    private final VideoDao videoDao;

    /**
     * Precompile statements to speed up queries.
     */
    private final PageableQuery<Video> findRelatedVideos;

    @Inject
    private KillrVideoTraversalSource traversalSource;

    /**
     * Wrap search queries with "paging":"driver" to dynamically enable
     * paging to ensure we pull back all available results in the application.
     * https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/cursorsDeepPaging.html#cursorsDeepPaging__using-paging-with-cql-solr-queries-solrquery-Rim2GsbY
     */
    private static final String PAGING_DRIVER_START = "{\"q\":\"";
    private static final String PAGING_DRIVER_END = "\", \"paging\":\"driver\"}";

    /**
     * Create a set of sentence conjunctions and other "undesirable"
     * words we will use later to exclude from search results.
     * Had to use .split() below because of the following conversation:
     * https://github.com/spring-projects/spring-boot/issues/501
     */
    @Value("#{'${killrvideo.search.ignoredWords}'.split(',')}")
    private final Set<String> ignoredWords = new HashSet<>();

    public SuggestedVideosRepository(CqlSession session, PageableQueryFactory pageableQueryFactory, VideoMapper mapper, VideoRowMapper videoRowMapper) {
        this.session = session;
        this.videoDao = mapper.getVideoDao();
        this.findRelatedVideos = pageableQueryFactory.newPageableQuery(
                QUERY_RELATED_VIDEOS,
                ConsistencyLevel.LOCAL_ONE,
                videoRowMapper::map
        );
    }

    /**
     * Get Pageable result for related video.
     **/
    public CompletableFuture<ResultListPage<Video>> getRelatedVideos(GetRelatedVideosRequestData request) {
        return findVideoById(request.getVideoid()).thenCompose(video -> {
            if (video == null) {
                throw new IllegalArgumentException(String.format("Video %s not found", request.getVideoid()));
            }
            String query = buildSolrQueryToSearchVideos(video);
            return findRelatedVideos.queryNext(
                    Optional.of(request.getPageSize()),
                    request.getPagingState(),
                    query
            );
        });
    }

    private CompletableFuture<Video> findVideoById(UUID videoId) {
        Assert.notNull(videoId, "videoid is required to update statistics");
        return this.videoDao.getVideoById(videoId);
    }

    /**
     * Perform a query using DSE Search to find other videos that are similar
     * to the "request" video using terms parsed from the name, tags,
     * and description columns of the "request" video.
     * <p>
     * The regex below will help us parse out individual words that we add to our
     * set. The set will automatically handle any duplicates that we parse out.
     * We can then use the end result termSet to query across the name, tags, and
     * description columns to find similar videos.
     */
    private String buildSolrQueryToSearchVideos(Video video) {
        final String space = " ";
        final String eachWordRegEx = "[^\\w]";
        final String eachWordPattern = Pattern.compile(eachWordRegEx).pattern();

        final HashSet<String> termSet = new HashSet<>(50);
        Collections.addAll(termSet, video.getName().toLowerCase().split(eachWordPattern));
        Collections.addAll(video.getTags()); // getTags already returns a set
        Collections.addAll(termSet, video.getDescription().toLowerCase().split(eachWordPattern));
        termSet.removeAll(ignoredWords);
        termSet.removeIf(String::isEmpty);

        final String delimitedTermList = termSet.stream().map(Object::toString).collect(Collectors.joining(","));
        LOGGER.debug("delimitedTermList is : " + delimitedTermList);

        final StringBuilder solrQuery = new StringBuilder();
        solrQuery.append(PAGING_DRIVER_START);
        solrQuery.append("name:(").append(delimitedTermList).append(")^2").append(space);
        solrQuery.append("tags:(").append(delimitedTermList).append(")^4").append(space);
        solrQuery.append("description:").append(delimitedTermList);
        solrQuery.append(PAGING_DRIVER_END);

        return solrQuery.toString();
    }

    /**
     * Search for videos.
     *
     * @param userid current userid,
     * @return Async Page
     */
    public CompletableFuture<List<Video>> getSuggestedVideosForUser(UUID userid) {
        // Parameters validation
        Assert.notNull(userid, "videoid is required to update statistics");
        // Build statement
        KillrVideoTraversal<Vertex, Map<String, Object>> graphTraversal = traversalSource.users(userid.toString())
                .recommendByUserRating(5, 4, 1000, 5);
        FluentGraphStatement graphStatement = FluentGraphStatement.newInstance(graphTraversal);
        //if (LOGGER.isDebugEnabled()) {
        //LOGGER.debug("Recommend TRAVERSAL is {} ",  DseUtils.displayGraphTranserval(graphTraversal));
        //}

        // Execute Sync
        CompletableFuture<AsyncGraphResultSet> futureRs = session.executeAsync(graphStatement).toCompletableFuture();

        // Mapping to expected List
        return futureRs.thenApply(
                rs -> StreamSupport.stream(rs.currentPage().spliterator(), false)
                        .map(this::mapGraphNode2Video).collect(Collectors.toList()
                        )
        );
    }

    private Video mapGraphNode2Video(GraphNode node) {
        GraphNode v = node.getByKey(VERTEX_VIDEO);
        GraphNode u = node.getByKey(VERTEX_USER);

        Video video = new Video();
        video.setAddedDate(v.getByKey("added_date").as(Instant.class));
        video.setName(v.getByKey("name").asString());
        video.setPreviewImageLocation(v.getByKey("preview_image_location").asString());
        video.setVideoid(v.getByKey("videoId").as(UUID.class));
        video.setUserid(u.getByKey("userId").as(UUID.class));
        return video;
    }

    /**
     * Subscription is done in dedicated service
     * {@link EventConsumerService}. (killrvideo-messaging)
     * <p>
     * Below we are using our KillrVideoTraversal DSL (Domain Specific Language)
     * to create our video vertex, then within add() we connect up the user responsible
     * for uploading the video with the "uploaded" edge, and then follow up with
     * any and all tags using the "taggedWith" edge.  Since we may have multiple
     * tags make sure to loop through and get them all in there.
     * <p>
     * Also note the use of add().  Take a look at Stephen's blog here
     * -> https://www.datastax.com/dev/blog/gremlin-dsls-in-java-with-dse-graph for more information.
     * This essentially allows us to chain multiple commands (uploaded and (n * taggedWith) in this case)
     * while "preserving" our initial video traversal position. Since the video vertex passes
     * through each step we do not need to worry about traversing back to video for each step
     * in the chain.
     * <p>
     * May be relevant to have a full sample traversal:
     * g.V().has("video","videoId", 6741b34e-03c7-4d83-bf55-deed496d6e03)
     * .fold()
     * .coalesce(__.unfold(),
     * __.addV("video")
     * .property("videoId",6741b34e-03c7-4d83-bf55-deed496d6e03))
     * .property("added_date",Thu Aug 09 11:00:44 CEST 2018)
     * .property("name","Paris JHipster Meetup #9")
     * .property("description","xxxxxx")
     * .property("preview_image_location","//img.youtube.com/vi/hOTjLOPXg48/hqdefault.jpg")
     * // Add Edge
     * .sideEffect(
     * __.as("^video").coalesce(
     * __.in("uploaded")
     * .hasLabel("user")
     * .has("userId",8a70e329-59f8-4e2e-aae8-1788c94e8410),
     * __.V()
     * .has("user","userId",8a70e329-59f8-4e2e-aae8-1788c94e8410)
     * .addE("uploaded")
     * .to("^video").inV())
     * )
     * // Tag with X (multiple times)
     * .sideEffect(
     * __.as("^video").coalesce(
     * __.out("taggedWith")
     * .hasLabel("tag")
     * .has("name","X"),
     * __.coalesce(
     * __.V().has("tag","name","X"),
     * __.addV("tag")
     * .property("name","X")
     * .property("tagged_date",Thu Aug 09 11:00:44 CEST 2018)
     * ).addE("taggedWith").from("^video").inV())
     * )
     * // Tag with FF4j
     * .sideEffect(
     * __.as("^video").coalesce(
     * __.out("taggedWith")
     * .hasLabel("tag")
     * .has("name","ff4j"),
     * __.coalesce(
     * __.V().has("tag","name","ff4j"),
     * __.addV("tag")
     * .property("name","ff4j")
     * .property("tagged_date",Thu Aug 09 11:00:44 CEST 2018))
     * .addE("taggedWith").from("^video").inV()))
     */
    public void updateGraphNewVideo(Video video) {
        final KillrVideoTraversal<Vertex, ?> traversal =
                // Add video Node
                traversalSource.video(video.getVideoid(), video.getName(), new Date(), video.getDescription(), video.getPreviewImageLocation())
                        // Add Uploaded Edge
                        .add(__.uploaded(video.getUserid()));
        // Add Tags Nodes and edges
        Sets.newHashSet(video.getTags()).forEach(tag -> traversal.add(__.taggedWith(tag, new Date())));
        /*
         * Now that our video is successfully applied lets
         * insert that video into our graph for the recommendation engine
         */
        FluentGraphStatement gStatement = FluentGraphStatement.newInstance(traversal);
        //LOGGER.info("Traversal for 'updateGraphNewVideo' : {}", DseUtils.displayGraphTranserval(traversal));
        session.executeAsync(gStatement).whenComplete((graphResultSet, ex) -> {
                    if (graphResultSet != null) {
                        LOGGER.debug("Added video vertex, uploaded, and taggedWith edges: " + graphResultSet.one());
                    } else {
                        //TODO: Potentially add some robustness code here
                        LOGGER.warn("Error handling YouTubeVideoAdded for graph: " + ex);
                    }
                }
        );
    }

    /**
     * Subscription is done in dedicated service
     * {@link EventConsumerService}. (killrvideo-messaging)
     * <p>
     * This will create a user vertex in our graph if it does not already exist.
     *
     * @param userId       current user
     * @param email        email.
     * @param userCreation user creation date.
     */
    public void updateGraphNewUser(UUID userId, String email, Date userCreation) {
        final KillrVideoTraversal<Vertex, Vertex> traversal = traversalSource.user(userId, email, userCreation);
        FluentGraphStatement gStatement = FluentGraphStatement.newInstance(traversal);
        //LOGGER.info("Executed transversal for 'updateGraphNewUser' : {}", DseUtils.displayGraphTranserval(traversal));
        session.executeAsync(gStatement).whenComplete((graphResultSet, ex) -> {
                    if (graphResultSet != null) {
                        LOGGER.debug("Added user vertex: " + graphResultSet.one());
                    } else {
                        LOGGER.warn("Error creating user vertex: " + ex);
                    }
                }
        );
    }

    /**
     * Subscription is done in dedicated service
     * {@link EventConsumerService}. (killrvideo-messaging)
     * <p>
     * Note that if either the user or video does not exist in the graph
     * the rating will not be applied nor will the user or video be
     * automatically created in this case.  This assumes both the user and video
     * already exist.
     */
    public void updateGraphNewUserRating(String videoId, UUID userId, int rate) {
        final KillrVideoTraversal<Vertex, ?> traversal = traversalSource.videos(videoId).add(__.rated(userId, rate));
        FluentGraphStatement gStatement = FluentGraphStatement.newInstance(traversal);
        //LOGGER.info("Executed transversal for 'updateGraphNewUserRating' : {}", DseUtils.displayGraphTranserval(traversal));
        session.executeAsync(gStatement).whenComplete((graphResultSet, ex) -> {
                    if (graphResultSet != null) {
                        LOGGER.debug("Added rating between user and video: " + graphResultSet.one());
                    } else {
                        //TODO: Potentially add some robustness code here
                        LOGGER.warn("Error Adding rating between user and video: " + ex);
                    }
                }
        );
    }
}
