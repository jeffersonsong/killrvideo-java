package com.killrvideo.utils

import com.killrvideo.utils.HashUtils.hashPassword
import com.killrvideo.utils.HashUtils.isPasswordValid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HashUtilsTest {
    @Test
    fun testIsPasswordValidWithEmptyRealPasswd() {
        assertFalse(isPasswordValid("", "hash"))
    }

    @Test
    fun testIsPasswordValidWithEmptyHash() {
        assertFalse(isPasswordValid("passwd", ""))
    }

    @Test
    fun testIsPasswordValidWithMismatchPassword() {
        assertFalse(isPasswordValid("passwd", "hash"))
    }

    @Test
    fun testIsPasswordValid() {
        val realPassword = "passwd"
        val hash: String = hashPassword(realPassword)
        assertTrue(isPasswordValid(realPassword, hash))
    }
}