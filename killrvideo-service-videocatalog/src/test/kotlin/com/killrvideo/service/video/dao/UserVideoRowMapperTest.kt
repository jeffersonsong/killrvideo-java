package com.killrvideo.service.video.dao

import com.datastax.oss.driver.api.core.cql.Row
import com.killrvideo.service.video.dto.Video
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class UserVideoRowMapperTest {
    private val mapper = UserVideoRowMapper()
    @Test
    fun testMap() {
        val userid = UUID.randomUUID()
        val videoid = UUID.randomUUID()
        val name = "Game"
        val previewLocation = "previewUrl"
        val addedDate = Instant.now()

        val row = mockk<Row>()
        every {row.getUuid(Video.COLUMN_USERID)} returns userid
        every {row.getUuid(Video.COLUMN_VIDEOID)} returns videoid
        every {row.getString(Video.COLUMN_NAME)} returns name
        every {row.getString(Video.COLUMN_PREVIEW)} returns previewLocation
        every {row.getInstant(Video.COLUMN_ADDED_DATE)} returns addedDate

        val result = mapper.map(row)
        Assertions.assertEquals(userid, result.userid)
        Assertions.assertEquals(videoid, result.videoid)
        Assertions.assertEquals(name, result.name)
        Assertions.assertEquals(previewLocation, result.previewImageLocation)
        Assertions.assertEquals(addedDate, result.addedDate)
    }
}