package com.killrvideo.service.comment.dao

import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.uuid.Uuids
import com.killrvideo.service.comment.dto.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class CommentRowMapperTest {
    private val mapper = CommentRowMapper()
    @Test
    fun testMap() {
        val comment = "Comment"
        val userid = UUID.randomUUID()
        val videoid = UUID.randomUUID()
        val commentid = Uuids.timeBased()
        val dateOfComment = Instant.now()

        val row = mockk<Row>()
        every { row.getString(Comment.COLUMN_COMMENT) } returns comment
        every { row.getUuid(Comment.COLUMN_USERID) } returns userid
        every { row.getUuid(Comment.COLUMN_COMMENTID) } returns commentid
        every { row.getUuid(Comment.COLUMN_VIDEOID) } returns videoid
        every { row.getInstant(Comment.COLUMN_COMMENT_TIMESTAMP) } returns dateOfComment

        val result = mapper.map(row)
        assertEquals(comment, result.comment)
        assertEquals(userid, result.userid)
        assertEquals(commentid, result.commentid)
        assertEquals(videoid, result.videoid)
        assertEquals(dateOfComment, result.dateOfComment)
    }
}
