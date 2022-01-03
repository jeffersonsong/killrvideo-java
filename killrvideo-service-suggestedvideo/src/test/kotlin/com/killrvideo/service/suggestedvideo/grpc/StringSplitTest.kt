package com.killrvideo.service.suggestedvideo.grpc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringSplitTest {
    @Test
    fun testSplitString() {
        val description = "Lego 2017 Police Staton"
        val eachWordPattern = "\\W+".toRegex()
        val result = description.lowercase().split(eachWordPattern)
        val expected = listOf("lego","2017","police", "staton")
        assertEquals(expected, result)
    }

    @Test
    fun testSplitString2() {
        val name= "x-wing-ucs.mp4"
        val eachWordPattern = "\\W+".toRegex()
        val result = name.lowercase().split(eachWordPattern)
        val expected = listOf("x","wing","ucs", "mp4")
        assertEquals(expected, result)
    }
}