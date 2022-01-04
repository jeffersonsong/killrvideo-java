package com.killrvideo.service.video.grpc

import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import killrvideo.video_catalog.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.stream.IntStream

internal class VideoCatalogServiceGrpcValidatorTest {
    private val validator = VideoCatalogServiceGrpcValidator()

    @Test
    fun testValidateGrpcRequest_submitYoutubeVideo_Success() {
        val request = submitYouTubeVideoRequest {
            videoId = randomUuid()
            userId = randomUuid()
            name = "Game"
            description = "Game"
            youTubeVideoId = "xyz123"
        }
        validator.validateGrpcRequest_submitYoutubeVideo(request)
    }

    @Test
    fun testValidateGrpcRequest_submitYoutubeVideo_Failure() {
        val request = submitYouTubeVideoRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_submitYoutubeVideo(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_getLatestPreviews_Success() {
        val request = getLatestVideoPreviewsRequest {
            pageSize = 2
        }
        validator.validateGrpcRequest_getLatestPreviews(request)
    }

    @Test
    fun testValidateGrpcRequest_getLatestPreviews_Failure() {
        val request = getLatestVideoPreviewsRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_getLatestPreviews(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_getVideo_Success() {
        val request = getVideoRequest {
            videoId = randomUuid()
        }
        validator.validateGrpcRequest_getVideo(request)
    }

    @Test
    fun testValidateGrpcRequest_getVideo_Failure() {
        val request = getVideoRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_getVideo(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_getVideoPreviews_Success() {
        val request = getVideoPreviewsRequest {
            videoIds.add(randomUuid())
        }
        validator.validateGrpcRequest_getVideoPreviews(request)
    }

    @Test
    fun testValidateGrpcRequest_getVideoPreviews_Failure() {
        val request = getVideoPreviewsRequest {
            IntStream.range(0, 21)
                .mapToObj { randomUuid() }
                .forEach {videoIds.add(it) }
        }
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_getVideoPreviews(request)
        }
    }

    @Test
    fun testValidateGrpcRequest_getUserVideoPreviews_Success() {
        val request = getUserVideoPreviewsRequest {
            userId =randomUuid()
            pageSize = 2
        }
        validator.validateGrpcRequest_getUserVideoPreviews(request)

    }

    @Test
    fun testValidateGrpcRequest_getUserVideoPreviews_Failure() {
        val request = getUserVideoPreviewsRequest {}
        assertThrows<IllegalArgumentException> {
            validator.validateGrpcRequest_getUserVideoPreviews(request)
        }
    }
}
