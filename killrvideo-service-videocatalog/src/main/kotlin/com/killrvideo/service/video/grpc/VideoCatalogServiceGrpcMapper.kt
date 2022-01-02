package com.killrvideo.service.video.grpc

import com.google.common.collect.Sets
import com.google.protobuf.Timestamp
import com.killrvideo.dse.dto.CustomPagingState
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.video.dto.LatestVideo
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.request.GetLatestVideoPreviewsRequestData
import com.killrvideo.service.video.request.GetUserVideoPreviewsRequestData
import com.killrvideo.utils.GrpcMappingUtils.*
import killrvideo.video_catalog.*
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Supplier
import java.util.stream.Collectors

/**
 * Utility mapping GRPC.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
class VideoCatalogServiceGrpcMapper {
    object SubmitYouTubeVideoRequestExtensions {
        fun SubmitYouTubeVideoRequest.parse(): Video =
            Video(
                videoid = fromUuid(this.videoId),
                userid = fromUuid(this.userId),
                name = this.name,
                description = this.description,
                location = this.youTubeVideoId,
                locationType = VideoLocationType.YOUTUBE.ordinal,
                previewImageLocation = "//img.youtube.com/vi/${this.youTubeVideoId}/hqdefault.jpg",
                tags = Sets.newHashSet(this.tagsList.iterator()),
                addedDate = null
            )
    }

    object GetUserVideoPreviewsRequestExtensions {
        fun GetUserVideoPreviewsRequest.parse() =
            GetUserVideoPreviewsRequestData(
                userId = fromUuid(this.userId),
                startingVideoId = if (this.hasStartingVideoId() && isNotBlank(this.startingVideoId.value)) {
                    fromUuid(this.startingVideoId)
                } else null,
                startingAddedDate = if (this.hasStartingAddedDate())
                    timestampToInstant(this.startingAddedDate)
                else null,
                pagingSize = if (this.pageSize > 0) this.pageSize else null,
                pagingState = if (isNotBlank(this.pagingState)) this.pagingState else null
            )
    }

    object GetLatestVideoPreviewsRequestExtensions {
        fun GetLatestVideoPreviewsRequest.parse(firstCustomPagingStateFactory: Supplier<CustomPagingState>):
                GetLatestVideoPreviewsRequestData =
            GetLatestVideoPreviewsRequestData(
                pageState = if (isNotBlank(this.pagingState))
                    CustomPagingState.deserialize(this.pagingState).orElse(firstCustomPagingStateFactory.get())
                else
                    firstCustomPagingStateFactory.get(),
                pageSize = this.pageSize,
                startDate = if (this.hasStartingAddedDate()) timestampToInstant(this.startingAddedDate) else null,
                startVideoId = if (this.hasStartingVideoId() && isNotBlank(this.startingVideoId.value))
                    fromUuid(this.startingVideoId) else null
            )
    }

    fun mapLatestVideoToGrpcResponse(returnedPage: LatestVideosPage): GetLatestVideoPreviewsResponse =
        getLatestVideoPreviewsResponse {
            videoPreviews.addAll(
                returnedPage.listOfPreview.stream()
                    .map { lv: LatestVideo -> mapLatestVideotoVideoPreview(lv) }
                    .collect(Collectors.toList())
            )
            returnedPage.nextPageState?.let { pagingState = it }
        }

    /**
     * Mapping to GRPC generated classes.
     */
    private fun mapLatestVideotoVideoPreview(v: LatestVideo): VideoPreview =
        videoPreview {
            v.addedDate?.let { addedDate = instantToTimeStamp(it) }
            v.name?.let { name = it }
            previewImageLocation = v.previewImageLocation ?: "N/A"
            v.userid?.let { userId = uuidToUuid(it) }
            v.videoid?.let { videoId = uuidToUuid(it) }
        }

    /**
     * Mapping to generated GPRC beans.
     */
    private fun mapFromUserVideotoVideoPreview(v: UserVideo): VideoPreview =
        videoPreview {
            v.addedDate?.let { addedDate = instantToTimeStamp(it) }
            v.name?.let { name = it }
            previewImageLocation = v.previewImageLocation ?: "N/A"
            v.userid?.let { userId = uuidToUuid(it) }
            v.videoid?.let { videoId = uuidToUuid(it) }
        }

    /**
     * Mapping to generated GPRC beans (Full detailed)
     */
    fun mapFromVideotoVideoResponse(v: Video): GetVideoResponse =
        // Check to see if any tags exist, if not, ensure to send an empty set instead of null
        getVideoResponse {
            v.addedDate?.let { addedDate = instantToTimeStamp(it) }
            v.description?.let { description = it }
            v.location?.let { location = it }
            v.locationType?.let { locationType = VideoLocationType.forNumber(it) }
            v.name?.let { name = it }
            v.userid?.let { userId = uuidToUuid(it) }
            v.videoid?.let { videoId = uuidToUuid(it) }
            v.tags?.let { tags.addAll(it) }
        }

    fun createYouTubeVideoAddedEvent(video: Video): YouTubeVideoAdded {
        val builder = YouTubeVideoAdded.newBuilder()
            .setAddedDate(Timestamp.newBuilder().build())

        video.description?.let { builder.setDescription(it) }
        video.location?.let { builder.setLocation(it) }
        video.name?.let { builder.setName(it) }
        video.previewImageLocation?.let { builder.setPreviewImageLocation(it) }
        video.userid?.let { builder.setUserId(uuidToUuid(it)) }
        video.videoid?.let { builder.setVideoId(uuidToUuid(it)) }

        return builder.build()
    }

    fun mapToGetVideoPreviewsResponse(videos: List<Video>): GetVideoPreviewsResponse =
        getVideoPreviewsResponse {
            videoPreviews.addAll(
                videos.stream().map { mapFromVideotoVideoPreview(it) }.collect(Collectors.toList())
            )
        }

    /**
     * Mapping to generated GPRC beans.
     */
    private fun mapFromVideotoVideoPreview(v: Video): VideoPreview =
        videoPreview {
            v.addedDate?.let { addedDate = instantToTimeStamp(it) }
            v.name?.let { name = it }
            previewImageLocation = v.previewImageLocation ?: "N/A"
            v.userid?.let { userId = uuidToUuid(it) }
            v.videoid?.let { videoId = uuidToUuid(it) }
        }

    fun mapToGetUserVideoPreviewsResponse(
        resultPage: ResultListPage<UserVideo?>,
        userid: UUID
    ): GetUserVideoPreviewsResponse =
        getUserVideoPreviewsResponse {
            userId = uuidToUuid(userid)
            resultPage.pagingState.ifPresent { pagingState = it }
            resultPage.results.stream()
                .filter { x -> x != null }
                .map { mapFromUserVideotoVideoPreview(it!!) }
                .forEach { videoPreviews.add(it) }
        }
}
