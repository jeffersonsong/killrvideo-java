package com.killrvideo.service.suggestedvideo.grpc

import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.suggestedvideo.dto.Video
import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpcMapper.GetRelatedVideosRequestExtensions.parse
import com.killrvideo.utils.GrpcMappingUtils
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class SuggestedVideosServiceGrpcMapperTest {
    private val mapper = SuggestedVideosServiceGrpcMapper()
    @Test
    fun testMapVideoAddedtoVideoDTO() {
        val v = video()
        val videoAdded = YouTubeVideoAdded.newBuilder()
            .setVideoId(GrpcMappingUtils.uuidToUuid(v.videoid))
            .setAddedDate(GrpcMappingUtils.instantToTimeStamp(v.addedDate))
            .setUserId(GrpcMappingUtils.uuidToUuid(v.userid))
            .setName(v.name)
            .addAllTags(v.tags)
            .setPreviewImageLocation(v.previewImageLocation)
            .setLocation(v.location)
            .build()
        val video = mapper.mapVideoAddedtoVideoDTO(videoAdded)
        assertEquals(v.videoid, video.videoid)
        assertEquals(v.userid, video.userid)
        assertEquals(v.addedDate, video.addedDate)
        assertEquals(v.name, video.name)
        assertEquals(v.tags!!.size, video.tags!!.size)
        assertEquals(v.previewImageLocation, video.previewImageLocation)
        assertEquals(v.location, video.location)
    }

    @Test
    fun testMapVideotoSuggestedVideoPreview() {
        val v = video()
        val preview = mapper.mapVideotoSuggestedVideoPreview(v)
        assertEquals(v.videoid.toString(), preview.videoId.value)
        assertEquals(v.userid.toString(), preview.userId.value)
        assertEquals(v.addedDate, GrpcMappingUtils.timestampToInstant(preview.addedDate))
        assertEquals(v.name, preview.name)
        assertEquals(v.previewImageLocation, preview.previewImageLocation)
    }

    @Test
    fun testParseGetRelatedVideosRequestData() {
        val videoid = UUID.randomUUID()
        val request = GetRelatedVideosRequest.newBuilder()
            .setVideoId(GrpcMappingUtils.uuidToUuid(videoid))
            .setPageSize(2)
            .setPagingState("Paging state")
            .build()
        val pojo = request.parse()
        assertEquals(videoid, pojo.videoid)
        assertEquals(2, pojo.pageSize)
        assertNotNull(pojo.pagingState)
        assertEquals("Paging state", pojo.pagingState)
    }

    @Test
    fun testMapToGetRelatedVideosResponse() {
        val v = video()
        val videoid = UUID.randomUUID()
        val resultPage = ResultListPage(listOf(v), "next page")
        val response = mapper.mapToGetRelatedVideosResponse(resultPage, videoid)
        assertEquals(videoid.toString(), response.videoId.value)
        assertEquals("next page", response.pagingState)
        assertEquals(1, response.videosCount)
    }

    private fun video(): Video {
        val videoid = UUID.randomUUID()
        val userid = UUID.randomUUID()
        val addedDate = Instant.now()
        val name = "Game"
        val tagList = Arrays.asList("tag1", "tag2")
        val previewLocation = "url"
        val location = "locationUrl"
        return Video(
            videoid = videoid,
            userid = userid,
            name = name,
            tags = HashSet(tagList),
            previewImageLocation = previewLocation,
            location=location,
            addedDate = addedDate
        )
    }
}
