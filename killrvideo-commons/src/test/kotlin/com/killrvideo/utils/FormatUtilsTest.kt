package com.killrvideo.utils

import com.google.protobuf.util.JsonFormat
import com.killrvideo.utils.FormatUtils.format
import com.killrvideo.utils.GrpcMappingUtils.instantToTimeStamp
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import killrvideo.video_catalog.events.VideoCatalogEvents
import killrvideo.video_catalog.events.uploadedVideoAccepted
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

internal class FormatUtilsTest {
    @Test
    fun testJsonFormat() {
        val event = sampleUploadedVideoAcceptedEvent()
        val text = format(event)

        val builder = VideoCatalogEvents.UploadedVideoAccepted.newBuilder()
        JsonFormat.parser().merge(text, builder)
        val result = builder.build()
        assertEquals(event.videoId, result.videoId)
        assertEquals(event.uploadUrl, result.uploadUrl)
        assertEquals(event.timestamp, result.timestamp)
    }

    private fun sampleUploadedVideoAcceptedEvent(): VideoCatalogEvents.UploadedVideoAccepted =
        uploadedVideoAccepted {
            videoId = randomUuid()
            uploadUrl = "https://www.youtube.com/xyz"
            timestamp = instantToTimeStamp(Instant.now())
        }
}
