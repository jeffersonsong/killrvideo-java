package com.killrvideo.service.suggestedvideo.repository

import com.datastax.dse.driver.api.core.graph.AsyncGraphResultSet
import com.datastax.dse.driver.api.core.graph.FluentGraphStatement
import com.datastax.dse.driver.api.core.graph.GraphNode
import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.Row
import com.google.common.collect.Sets
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.dse.graph.KillrVideoTraversalConstants
import com.killrvideo.dse.graph.KillrVideoTraversalSource
import com.killrvideo.dse.graph.`__` as underscore
import com.killrvideo.dse.utils.PageableQuery
import com.killrvideo.dse.utils.PageableQueryFactory
import com.killrvideo.service.suggestedvideo.dao.*
import com.killrvideo.service.suggestedvideo.dto.Video
import com.killrvideo.service.suggestedvideo.request.GetRelatedVideosRequestData
import io.grpc.Status
import kotlinx.coroutines.future.await
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.util.Assert
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import javax.inject.Inject

/**
 * Implementations of operation for Videos.
 *
 * @author DataStax Developer Advocates team.
 */
@Repository
class SuggestedVideosRepository(
    private val session: CqlSession, pageableQueryFactory: PageableQueryFactory, mapper: VideoMapper,
    @Qualifier("suggestedVideoRowMapper") videoRowMapper: VideoRowMapper,
    @Inject
    private val traversalSource: KillrVideoTraversalSource
) {
    private val videoDao: VideoDao = mapper.videoDao

    /**
     * Precompile statements to speed up queries.
     */
    private val findRelatedVideos: PageableQuery<Video?>

    /**
     * Create a set of sentence conjunctions and other "undesirable"
     * words we will use later to exclude from search results.
     * Had to use .split() below because of the following conversation:
     * https://github.com/spring-projects/spring-boot/issues/501
     */
    @Value("#{'\${killrvideo.search.ignoredWords}'.split(',')}")
    private val ignoredWords: Set<kotlin.String> = HashSet()

    init {
        findRelatedVideos = pageableQueryFactory.newPageableQuery(
            QUERY_RELATED_VIDEOS,
            ConsistencyLevel.LOCAL_ONE
        ) { row: Row -> videoRowMapper.map(row) }
    }

    /**
     * Get Pageable result for related video.
     */
    suspend fun getRelatedVideos(request: GetRelatedVideosRequestData): ResultListPage<Video?> {
        return findVideoById(request.videoid)
            .thenCompose { video: Video? ->
                if (video == null) {
                    throw CompletionException(Status.NOT_FOUND
                        .withDescription("Video ${request.videoid} not found").asRuntimeException()
                    )
                } else {
                    val query = buildSolrQueryToSearchVideos(video)
                    findRelatedVideos.queryNext(
                        Optional.of(request.pageSize),
                        Optional.ofNullable(request.pagingState),
                        query
                    )
                }
            }.await()
    }

    private fun findVideoById(videoId: UUID): CompletableFuture<Video?> {
        Assert.notNull(videoId, "videoid is required to update statistics")
        return videoDao.getVideoById(videoId)
    }

    /**
     * Perform a query using DSE Search to find other videos that are similar
     * to the "request" video using terms parsed from the name, tags,
     * and description columns of the "request" video.
     *
     *
     * The regex below will help us parse out individual words that we add to our
     * set. The set will automatically handle any duplicates that we parse out.
     * We can then use the end result termSet to query across the name, tags, and
     * description columns to find similar videos.
     */
    private fun buildSolrQueryToSearchVideos(video: Video): kotlin.String {
        val space = " "
        val termSet = HashSet<kotlin.String>(50)
        video.name ?.let {sentence ->
            splitSentence(sentence).forEach { termSet.add(it)}
        }
        video.description ?.let {sentence ->
            splitSentence(sentence).forEach { termSet.add(it)}
        }
        termSet.removeAll(ignoredWords)
        termSet.removeIf { it.isEmpty() }
        val delimitedTermList = termSet.stream().map { obj: kotlin.String -> obj }.collect(Collectors.joining(","))
        LOGGER.debug("delimitedTermList is : $delimitedTermList")
        val solrQuery = StringBuilder()
        solrQuery.append(PAGING_DRIVER_START)
        solrQuery.append("name:(").append(delimitedTermList).append(")^2").append(space)
        solrQuery.append("tags:(").append(delimitedTermList).append(")^4").append(space)
        solrQuery.append("description:").append(delimitedTermList)
        solrQuery.append(PAGING_DRIVER_END)
        return solrQuery.toString()
    }

    private fun splitSentence(sentence: String): List<String> {
        val eachWordPattern = "\\W+".toRegex()
        return sentence.lowercase(Locale.getDefault()).split(eachWordPattern)
    }

    /**
     * Search for videos.
     *
     * @param userid current userid,
     * @return Async Page
     */
    suspend fun getSuggestedVideosForUser(userid: UUID): List<Video> {
        // Build statement
        val graphTraversal = traversalSource.users(userid.toString())
            .recommendByUserRating(5, 4, 1000, 5)
        val graphStatement = FluentGraphStatement.newInstance(graphTraversal)
        //if (LOGGER.isDebugEnabled()) {
        //LOGGER.debug("Recommend TRAVERSAL is {} ",  DseUtils.displayGraphTranserval(graphTraversal));
        //}

        // Execute Sync
        val futureRs = session.executeAsync(graphStatement).toCompletableFuture()

        // Mapping to expected List
        return futureRs.thenApply { rs: AsyncGraphResultSet ->
            StreamSupport.stream(rs.currentPage().spliterator(), false)
                .map { node: GraphNode -> mapGraphNode2Video(node) }.collect(
                    Collectors.toList()
                )
        }.await()
    }

    private fun mapGraphNode2Video(node: GraphNode): Video {
        val v = node.getByKey(KillrVideoTraversalConstants.VERTEX_VIDEO)
        val u = node.getByKey(KillrVideoTraversalConstants.VERTEX_USER)
        return Video(
            addedDate = v.getByKey("added_date") as Instant,
            name = v.getByKey("name").asString(),
            previewImageLocation = v.getByKey("preview_image_location").asString(),
            videoid = v.getByKey("videoId") as UUID,
            userid = u.getByKey("userId") as UUID
        )
    }

    /**
     * Subscription is done in dedicated service
     * [EventConsumerService]. (killrvideo-messaging)
     *
     *
     * Below we are using our KillrVideoTraversal DSL (Domain Specific Language)
     * to create our video vertex, then within add() we connect up the user responsible
     * for uploading the video with the "uploaded" edge, and then follow up with
     * any and all tags using the "taggedWith" edge.  Since we may have multiple
     * tags make sure to loop through and get them all in there.
     *
     *
     * Also note the use of add().  Take a look at Stephen's blog here
     * -> https://www.datastax.com/dev/blog/gremlin-dsls-in-java-with-dse-graph for more information.
     * This essentially allows us to chain multiple commands (uploaded and (n * taggedWith) in this case)
     * while "preserving" our initial video traversal position. Since the video vertex passes
     * through each step we do not need to worry about traversing back to video for each step
     * in the chain.
     *
     *
     * May be relevant to have a full sample traversal:
     * g.V().has("video","videoId", 6741b34e-03c7-4d83-bf55-deed496d6e03)
     * .fold()
     * .coalesce(underscore.unfold(),
     * underscore.addV("video")
     * .property("videoId",6741b34e-03c7-4d83-bf55-deed496d6e03))
     * .property("added_date",Thu Aug 09 11:00:44 CEST 2018)
     * .property("name","Paris JHipster Meetup #9")
     * .property("description","xxxxxx")
     * .property("preview_image_location","//img.youtube.com/vi/hOTjLOPXg48/hqdefault.jpg")
     * // Add Edge
     * .sideEffect(
     * underscore.as("^video").coalesce(
     * underscore.in("uploaded")
     * .hasLabel("user")
     * .has("userId",8a70e329-59f8-4e2e-aae8-1788c94e8410),
     * underscore.V()
     * .has("user","userId",8a70e329-59f8-4e2e-aae8-1788c94e8410)
     * .addE("uploaded")
     * .to("^video").inV())
     * )
     * // Tag with X (multiple times)
     * .sideEffect(
     * underscore.as("^video").coalesce(
     * underscore.out("taggedWith")
     * .hasLabel("tag")
     * .has("name","X"),
     * underscore.coalesce(
     * underscore.V().has("tag","name","X"),
     * underscore.addV("tag")
     * .property("name","X")
     * .property("tagged_date",Thu Aug 09 11:00:44 CEST 2018)
     * ).addE("taggedWith").from("^video").inV())
     * )
     * // Tag with FF4j
     * .sideEffect(
     * underscore.as("^video").coalesce(
     * underscore.out("taggedWith")
     * .hasLabel("tag")
     * .has("name","ff4j"),
     * underscore.coalesce(
     * underscore.V().has("tag","name","ff4j"),
     * underscore.addV("tag")
     * .property("name","ff4j")
     * .property("tagged_date",Thu Aug 09 11:00:44 CEST 2018))
     * .addE("taggedWith").from("^video").inV()))
     */
    fun updateGraphNewVideo(video: Video) {
        val traversal =  // Add video Node
            traversalSource.video(
                video.videoid,
                video.name,
                Date(),
                video.description,
                video.previewImageLocation
            ) // Add Uploaded Edge
                .add(underscore.uploaded<Vertex>(video.userid))
        // Add Tags Nodes and edges
        Sets.newHashSet(video.tags!!).forEach { tag: String -> traversal.add(underscore.taggedWith<Vertex>(tag, Date())) }
        /*
         * Now that our video is successfully applied lets
         * insert that video into our graph for the recommendation engine
         */
        val gStatement = FluentGraphStatement.newInstance(traversal)
        //LOGGER.info("Traversal for 'updateGraphNewVideo' : {}", DseUtils.displayGraphTranserval(traversal));
        session.executeAsync(gStatement).whenComplete { graphResultSet: AsyncGraphResultSet?, ex: Throwable ->
            if (graphResultSet != null) {
                LOGGER.debug("Added video vertex, uploaded, and taggedWith edges: " + graphResultSet.one())
            } else {
                //TODO: Potentially add some robustness code here
                LOGGER.warn("Error handling YouTubeVideoAdded for graph: $ex")
            }
        }
    }

    /**
     * Subscription is done in dedicated service
     * [EventConsumerService]. (killrvideo-messaging)
     *
     *
     * This will create a user vertex in our graph if it does not already exist.
     *
     * @param userId       current user
     * @param email        email.
     * @param userCreation user creation date.
     */
    fun updateGraphNewUser(userId: UUID, email: kotlin.String, userCreation: Date) {
        val traversal = traversalSource.user(userId, email, userCreation)
        val gStatement = FluentGraphStatement.newInstance(traversal)
        //LOGGER.info("Executed transversal for 'updateGraphNewUser' : {}", DseUtils.displayGraphTranserval(traversal));
        session.executeAsync(gStatement).whenComplete { graphResultSet: AsyncGraphResultSet?, ex: Throwable ->
            if (graphResultSet != null) {
                LOGGER.debug("Added user vertex: " + graphResultSet.one())
            } else {
                LOGGER.warn("Error creating user vertex: $ex")
            }
        }
    }

    /**
     * Subscription is done in dedicated service
     * [EventConsumerService]. (killrvideo-messaging)
     *
     *
     * Note that if either the user or video does not exist in the graph
     * the rating will not be applied nor will the user or video be
     * automatically created in this case.  This assumes both the user and video
     * already exist.
     */
    fun updateGraphNewUserRating(videoId: String, userId: UUID, rate: Int) {
        val traversal = traversalSource.videos(videoId).add(underscore.rated<Any>(userId, rate))
        val gStatement = FluentGraphStatement.newInstance(traversal)
        //LOGGER.info("Executed transversal for 'updateGraphNewUserRating' : {}", DseUtils.displayGraphTranserval(traversal));
        session.executeAsync(gStatement).whenComplete { graphResultSet: AsyncGraphResultSet?, ex: Throwable ->
            if (graphResultSet != null) {
                LOGGER.debug("Added rating between user and video: " + graphResultSet.one())
            } else {
                //TODO: Potentially add some robustness code here
                LOGGER.warn("Error Adding rating between user and video: $ex")
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SuggestedVideosRepository::class.java)
        private const val QUERY_RELATED_VIDEOS = "SELECT * " +
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
