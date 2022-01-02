package com.killrvideo.service.video.grpc

import com.killrvideo.utils.FluentValidator
import killrvideo.common.CommonTypes
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.function.Consumer

/**
 * Validate arguments in GRPC services
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
class VideoCatalogServiceGrpcValidator {
    /**
     * Validate arguments for 'SubmitYouTubeVideo'
     */
    fun validateGrpcRequest_submitYoutubeVideo(request: SubmitYouTubeVideoRequest) =
        FluentValidator.of("submitVideo", request, LOGGER)
            .notEmpty("video id", StringUtils.isBlank(request.videoId.value))
            .notEmpty("user id", StringUtils.isBlank(request.userId.value))
            .notEmpty("video name", StringUtils.isBlank(request.name))
            .notEmpty("video description", StringUtils.isBlank(request.description))
            .notEmpty("video youtube id", StringUtils.isBlank(request.youTubeVideoId))
            .validate()

    /**
     * Validate arguments for 'getLatestVideoPreview'
     */
    fun validateGrpcRequest_getLatestPreviews(request: GetLatestVideoPreviewsRequest) =
        FluentValidator.of("getLatestVideoPreviews", request, LOGGER)
            .positive("page size", request.pageSize == 0)
            .validate()

    fun validateGrpcRequest_getVideo(request: GetVideoRequest) =
        FluentValidator.of("getVideo", request, LOGGER)
            .notEmpty("video id", StringUtils.isBlank(request.videoId.value))
            .validate()

    fun validateGrpcRequest_getVideoPreviews(request: GetVideoPreviewsRequest) {
        val validator = FluentValidator.of("getVideoPreview", request, LOGGER)
            .error(
                "cannot get more than 20 videos at once for get video previews request",
                request.videoIdsCount >= 20
            )
        request.videoIdsList.forEach(
            Consumer { uuid: CommonTypes.Uuid? ->
                validator.error(
                    "provided UUID values cannot be null or blank for get video previews request",
                    uuid == null || StringUtils.isBlank(uuid.value)
                )
            }
        )
        validator.validate()
    }

    fun validateGrpcRequest_getUserVideoPreviews(request: GetUserVideoPreviewsRequest) =
        FluentValidator.of("getUserVideoPreview", request, LOGGER)
            .notEmpty("user id", StringUtils.isBlank(request.userId.value))
            .positive("page size", request.pageSize == 0)
            .validate()

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VideoCatalogServiceGrpcValidator::class.java)
    }
}
