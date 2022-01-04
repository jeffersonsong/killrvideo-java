package com.killrvideo.service.video.grpc

import com.killrvideo.dse.dto.CustomPagingState
import com.killrvideo.exception.NotFoundException
import com.killrvideo.messaging.dao.MessagingDao
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpcMapper.GetLatestVideoPreviewsRequestExtensions.parse
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpcMapper.GetUserVideoPreviewsRequestExtensions.parse
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpcMapper.SubmitYouTubeVideoRequestExtensions.parse
import com.killrvideo.service.video.repository.VideoCatalogRepository
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import killrvideo.video_catalog.VideoCatalogServiceGrpcKt
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*
import killrvideo.video_catalog.getVideoPreviewsResponse
import killrvideo.video_catalog.submitYouTubeVideoResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.stream.Collectors

/**
 * Exposition of comment services with GPRC Technology & Protobuf Interface
 *
 * @author DataStax Developer Advocates team.
 */
@Service
class VideoCatalogServiceGrpc(
    private val videoCatalogRepository: VideoCatalogRepository,
    private val messagingDao: MessagingDao,
    private val validator: VideoCatalogServiceGrpcValidator,
    private val mapper: VideoCatalogServiceGrpcMapper,
    @Value("\${killrvideo.discovery.services.videoCatalog : VideoCatalogService}")
    val serviceKey: String,
    @Value("\${killrvideo.messaging.destinations.youTubeVideoAdded : topic-kv-videoCreation}")
    private val topicVideoCreated: String
) : VideoCatalogServiceGrpcKt.VideoCatalogServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    /**
     * {@inheritDoc}
     */
    override suspend fun submitYouTubeVideo(request: SubmitYouTubeVideoRequest): SubmitYouTubeVideoResponse {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_submitYoutubeVideo(request)

        // Mapping GRPC => Domain (Dao)
        val video = request.parse()

        logger.debug { "Insert youtube video ${video.videoid} for user ${video.userid} : $video" }

        // Execute query (ASYNC)
        return kotlin.runCatching { videoCatalogRepository.insertVideoAsync(video) }
            .mapCatching { rs ->
                notifyYoutubeVideoAdded(video)
                rs
            }
            .map { submitYouTubeVideoResponse {}}
            .getOrThrow()
    }

    private fun notifyYoutubeVideoAdded(video: Video) =
        messagingDao.sendEvent(topicVideoCreated, mapper.createYouTubeVideoAddedEvent(video)).get()

    /**
     * Get the latest video (Home Page)
     *
     *
     * In this method, we craft our own paging state. The custom paging state format is:
     * <br></br>
     * <br></br>
     * `
     * yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd,<index>,<Cassandra paging state as string>
    ` *
     * <br></br>
     * <br></br>
     *
     *  * The first field is the date of 7 days in the past, starting from **now**
     *  * The second field is the index in this date list, to know at which day in the past we stop at the previous query
     *  * The last field is the serialized form of the native Cassandra paging state
     *
     *
     *
     * On the first query, we create our own custom paging state in the server by computing the list of 8 days
     * in the past, the **index** is set to 0 and there is no native Cassandra paging state
     *
     *
     * <br></br>
     * On subsequent request, we decode the custom paging state coming from the web app and resume querying from
     * the appropriate date, and we inject also the native Cassandra paging state.
     * <br></br>
     * **However, we can only use the native Cassandra paging state for the 1st query in the for loop. Indeed
     * Cassandra paging state is a hash of query string and bound values. We may switch partition to move one day
     * back in the past to fetch more results so the paging state will no longer be usable**]
     */
    override suspend fun getLatestVideoPreviews(request: GetLatestVideoPreviewsRequest): GetLatestVideoPreviewsResponse {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_getLatestPreviews(request)

        // GRPC Parameters Mappings
        val requestData = request.parse { CustomPagingState.buildFirstCustomPagingState() }
        return runCatching { videoCatalogRepository.getLatestVideoPreviewsAsync(requestData) }
            .map { mapper.mapLatestVideoToGrpcResponse(it)}
            .getOrThrow()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getVideo(request: GetVideoRequest): GetVideoResponse {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_getVideo(request)

        // GRPC Parameters Mappings
        val videoId = fromUuid(request.videoId)

        // Invoke Async
        return runCatching { videoCatalogRepository.getVideoById(videoId) }
            .mapCatching { video ->
                video ?: throw NotFoundException("Video with id $videoId was not found")
                video
            }.map { mapper.mapFromVideotoVideoResponse(it) }
            .getOrThrow()
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getVideoPreviews(request: GetVideoPreviewsRequest): GetVideoPreviewsResponse {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_getVideoPreviews(request)

        return if (request.videoIdsCount == 0) {
            logger.warn { "No video id provided for video preview" }
            getVideoPreviewsResponse {}

        } else {
            // GRPC Parameters Mappings
            val listOfVideoIds = request.videoIdsList.stream().map { fromUuid(it) }.collect(Collectors.toList())

            // Execute Async
            runCatching { videoCatalogRepository.getVideoPreview(listOfVideoIds) }
                .map { mapper.mapToGetVideoPreviewsResponse(it) }
                .getOrThrow()
        }
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun getUserVideoPreviews(request: GetUserVideoPreviewsRequest): GetUserVideoPreviewsResponse {
        // GRPC Parameters Validation
        validator.validateGrpcRequest_getUserVideoPreviews(request)

        // GRPC Parameters Mappings
        val requestData = request.parse()
        // Map Result back to GRPC
        return kotlin.runCatching { videoCatalogRepository.getUserVideosPreview(requestData) }
            .map { mapper.mapToGetUserVideoPreviewsResponse(it, requestData.userId) }
            .getOrThrow()
    }
}
