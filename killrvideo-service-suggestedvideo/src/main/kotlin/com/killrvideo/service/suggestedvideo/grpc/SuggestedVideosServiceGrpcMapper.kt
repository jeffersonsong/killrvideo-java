package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.suggestedvideo.dto.Video
import com.killrvideo.service.suggestedvideo.request.GetRelatedVideosRequestData
import com.killrvideo.utils.GrpcMappingUtils.fromUuid
import com.killrvideo.utils.GrpcMappingUtils.instantToTimeStamp
import com.killrvideo.utils.GrpcMappingUtils.timestampToInstant
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.suggested_videos.SuggestedVideosService.*
import killrvideo.suggested_videos.getRelatedVideosResponse
import killrvideo.suggested_videos.getSuggestedForUserResponse
import killrvideo.suggested_videos.suggestedVideoPreview
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.springframework.stereotype.Component
import java.util.*

/**
 * Helper and mappers for DAO <=> GRPC Communications
 *
 * @author DataStax Developer Advocates Team
 */
@Component
class SuggestedVideosServiceGrpcMapper {
    object GetRelatedVideosRequestExtensions {
        fun GetRelatedVideosRequest.parse(): GetRelatedVideosRequestData =
            GetRelatedVideosRequestData(
                videoid = fromUuid(this.videoId),
                pageSize = this.pageSize,
                pagingState = if (isNotBlank(this.pagingState)) this.pagingState else null
            )
    }

    fun mapVideoAddedtoVideoDTO(videoAdded: YouTubeVideoAdded): Video =
        // Convert Stub to Dto, dao must not be related to interface GRPC
        Video(
            videoid = fromUuid(videoAdded.videoId),
            addedDate = timestampToInstant(videoAdded.addedDate),
            userid = fromUuid(videoAdded.userId),
            name = videoAdded.name,
            tags = HashSet(videoAdded.tagsList),
            previewImageLocation = videoAdded.previewImageLocation,
            location = videoAdded.location
        )

    /**
     * Mapping to generated GPRC beans. (Suggested videos special)
     */
    fun mapVideotoSuggestedVideoPreview(v: Video): SuggestedVideoPreview =
        suggestedVideoPreview {
            v.name?.let { name = it }
            v.videoid?.let { videoId = uuidToUuid(it) }
            v.userid?.let { userId = uuidToUuid(it) }
            v.previewImageLocation?.let { previewImageLocation = it }
            v.addedDate?.let { addedDate = instantToTimeStamp(it) }
        }


    fun mapToGetSuggestedForUserResponse(_userid: UUID, _videos: List<Video>): GetSuggestedForUserResponse =
        getSuggestedForUserResponse {
            userId = uuidToUuid(_userid)
            _videos.stream().map { mapVideotoSuggestedVideoPreview(it) }.forEach {
                videos.add(it)
            }
        }

    fun mapToGetRelatedVideosResponse(resultPage: ResultListPage<Video>, _videoid: UUID): GetRelatedVideosResponse {
        val videoGrpcUUID = uuidToUuid(_videoid)
        return getRelatedVideosResponse {
            videoId = videoGrpcUUID

            resultPage.results.stream()
                .filter { it != null }
                .map { mapVideotoSuggestedVideoPreview(it!!) }
                .filter { it.videoId != videoGrpcUUID }
                .forEach { videos.add(it) }

            resultPage.pagingState?.let { pagingState = it }
        }
    }
}
