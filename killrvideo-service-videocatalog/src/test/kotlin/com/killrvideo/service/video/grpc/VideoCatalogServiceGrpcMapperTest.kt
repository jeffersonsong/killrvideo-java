package com.killrvideo.service.video.grpc

import com.killrvideo.dse.dto.CustomPagingState
import com.killrvideo.dse.dto.ResultListPage
import com.killrvideo.service.video.dto.LatestVideo
import com.killrvideo.service.video.dto.LatestVideosPage
import com.killrvideo.service.video.dto.UserVideo
import com.killrvideo.service.video.dto.Video
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpcMapper.GetLatestVideoPreviewsRequestExtensions.parse
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpcMapper.GetUserVideoPreviewsRequestExtensions.parse
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpcMapper.SubmitYouTubeVideoRequestExtensions.parse
import com.killrvideo.utils.GrpcMappingUtils.instantToTimeStamp
import com.killrvideo.utils.GrpcMappingUtils.uuidToUuid
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoLocationType
import killrvideo.video_catalog.getLatestVideoPreviewsRequest
import killrvideo.video_catalog.getUserVideoPreviewsRequest
import killrvideo.video_catalog.submitYouTubeVideoRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

internal class VideoCatalogServiceGrpcMapperTest {
    private val mapper = VideoCatalogServiceGrpcMapper()

    @Test
    fun testMapSubmitYouTubeVideoRequestAsVideo() {
        val v = video()
        val request = submitYouTubeVideoRequest {
            videoId = uuidToUuid(v.videoid)
            userId = uuidToUuid(v.userid)
            name = v.name!!
            description = v.description!!
            youTubeVideoId = v.location!!
            tags.addAll(v.tags!!)
        }
        val video = request.parse()
        assertEquals(v.videoid, video.videoid)
        assertEquals(v.userid, video.userid)
        assertEquals(v.name, video.name)
        assertEquals(v.location, video.location)
        assertEquals(v.description, video.description)
        assertEquals(v.tags!!.size, video.tags!!.size)
        assertEquals(v.locationType, video.locationType)
    }

    @Test
    fun testMapLatestVideoToGrpcResponse() {
        val v = video()
        val latestVideo = LatestVideo.from(v, Instant.now())
        val nextPageState = "next page state"
        val cassandraPagingState = "cassandra paging state"
        val latestVideosPage = LatestVideosPage(
            listOfPreview = mutableListOf(latestVideo),
            cassandraPagingState = cassandraPagingState,
            nextPageState = nextPageState
        )
        val response = mapper.mapLatestVideoToGrpcResponse(latestVideosPage)
        assertEquals(nextPageState, response.pagingState)
        assertEquals(1, response.videoPreviewsCount)
    }

    @Test
    fun testMapFromVideotoVideoResponse() {
        val v = video()
        val response = mapper.mapFromVideotoVideoResponse(v)
        assertEquals(v.videoid.toString(), response.videoId.value)
        assertEquals(v.description, response.description)
        assertEquals(v.location, response.location)
        assertEquals(v.locationType, response.locationType.number)
        assertEquals(v.name, response.name)
        assertEquals(v.userid.toString(), response.userId.value)
        assertEquals(v.tags!!.size, response.tagsCount)
    }

    @Test
    fun testCreateYouTubeVideoAddedEvent() {
        val v = video()
        val event = mapper.createYouTubeVideoAddedEvent(v)
        assertEquals(v.videoid.toString(), event.videoId.value)
        assertEquals(v.description, event.description)
        assertEquals(v.location, event.location)
        assertEquals(v.name, event.name)
        assertEquals(v.userid.toString(), event.userId.value)
    }

    @Test
    fun testParseGetLatestVideoPreviewsRequest() {
        val startingVidoId = UUID.randomUUID()
        val request = getLatestVideoPreviewsRequest {
            pageSize = 2
            startingVideoId = uuidToUuid(startingVidoId)
        }
        val pojo = request.parse { CustomPagingState.buildFirstCustomPagingState() }
        assertEquals(2, pojo.pageSize)
        assertNotNull(pojo.startVideoId)
        assertEquals(startingVidoId, pojo.startVideoId)
    }

    @Test
    fun testMapToGetVideoPreviewsResponse() {
        val v = video()
        val response = mapper.mapToGetVideoPreviewsResponse(listOf(v))
        assertEquals(1, response.videoPreviewsCount)
        val preview = response.getVideoPreviews(0)
        assertEquals(v.name, preview.name)
        assertEquals(v.userid.toString(), preview.userId.value)
        assertEquals(v.videoid.toString(), preview.videoId.value)
    }

    @Test
    fun testParseGetUserVideoPreviewsRequest() {
        val userid = UUID.randomUUID()
        val startingVideoid = UUID.randomUUID()
        val startingDate = Instant.now().minus(1L, ChronoUnit.DAYS)
        val pagesize = 2
        val pagingstate = "Paging state"
        val request = getUserVideoPreviewsRequest {
            userId = uuidToUuid(userid)
            startingVideoId = uuidToUuid(startingVideoid)
            startingAddedDate = instantToTimeStamp(startingDate)
            pageSize = pagesize
            pagingState = pagingstate
        }
        val pojo = request.parse()
        assertEquals(userid, pojo.userId)
        assertNotNull(pojo.startingVideoId)
        assertEquals(startingVideoid, pojo.startingVideoId)
        assertNotNull(pojo.startingAddedDate)
        assertEquals(startingDate, pojo.startingAddedDate)
        assertNotNull(pojo.pagingSize)
        assertEquals(pagesize, pojo.pagingSize)
        assertNotNull(pojo.pagingState)
        assertEquals(pagingstate, pojo.pagingState)
    }

    @Test
    fun testMapToGetUserVideoPreviewsResponse() {
        val userid = UUID.randomUUID()
        val v = video()
        val userVideo = UserVideo.from(v, Instant.now())
        val pagingState = "Paging state"
        val resultListPage = ResultListPage(
            listOf(userVideo),
            Optional.of(pagingState)
        )
        val response = mapper.mapToGetUserVideoPreviewsResponse(resultListPage, userid)
        assertEquals(pagingState, response.pagingState)
        assertEquals(1, response.videoPreviewsCount)
    }

    private fun video(): Video =
        Video(
            videoid = UUID.randomUUID(),
            userid = UUID.randomUUID(),
            name = "Game",
            location = "url",
            description = "Description",
            tags = mutableSetOf("tag1", "tag2"),
            previewImageLocation = "previewUrl",
            locationType = VideoLocationType.YOUTUBE.ordinal,
            addedDate = Instant.now()
        )
}
