package com.killrvideo.service.video.dao

import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.service.video.dto.LatestVideo
import com.killrvideo.service.video.dto.Video
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class LatestVideoRowMapperTest {
    private val mapper = LatestVideoRowMapper()
    @Test
    fun testMap() {
        val yyyymmdd = "20211231"
        val userid = UUID.randomUUID()
        val videoid = UUID.randomUUID()
        val name = "Game"
        val previewLocation = "previewUrl"
        val addedDate = Instant.now()

        val row = mockk<Row>()
        every {row.getString(LatestVideo.COLUMN_YYYYMMDD)} returns yyyymmdd
        every {row.getUuid(Video.COLUMN_USERID)} returns userid
        every {row.getUuid(Video.COLUMN_VIDEOID)} returns videoid
        every {row.getString(Video.COLUMN_NAME)} returns name
        every {row.getString(Video.COLUMN_PREVIEW)} returns previewLocation
        every {row.getInstant(Video.COLUMN_ADDED_DATE)} returns addedDate

        val result = mapper.map(row)
        assertEquals(yyyymmdd, result.yyyymmdd)
        assertEquals(userid, result.userid)
        assertEquals(videoid, result.videoid)
        assertEquals(name, result.name)
        assertEquals(previewLocation, result.previewImageLocation)
        assertEquals(addedDate, result.addedDate)
    }
}
