package com.killrvideo.utils

import com.google.protobuf.TextFormat
import com.google.protobuf.util.JsonFormat
import com.killrvideo.utils.GrpcMappingUtils.instantToTimeStamp
import com.killrvideo.utils.GrpcMappingUtils.randomUuid
import killrvideo.video_catalog.events.VideoCatalogEvents
import killrvideo.video_catalog.events.uploadedVideoAccepted
import org.junit.jupiter.api.Test
import java.time.Instant

class ProtoTextFormatTest {
    @Test
    fun testTextFormat() {
        val event = sampleUploadedVideoAcceptedEvent()
        val text = TextFormat.printer().shortDebugString(event)
        print(text)
    }

    @Test
    fun testJsonFormat() {
        val event = sampleUploadedVideoAcceptedEvent()
        val text = JsonFormat.printer().print(event).replace("\\s+".toRegex(), " ")
        print(text)
    }

    private fun sampleUploadedVideoAcceptedEvent(): VideoCatalogEvents.UploadedVideoAccepted {
        return uploadedVideoAccepted {
            videoId = randomUuid()
            uploadUrl = "https://www.youtube.com/xyz"
            timestamp = instantToTimeStamp(Instant.now())
        }
    }
}
