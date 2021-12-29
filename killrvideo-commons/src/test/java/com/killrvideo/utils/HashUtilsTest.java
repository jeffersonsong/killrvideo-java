package com.killrvideo.utils;

import org.junit.jupiter.api.Test;

import static com.killrvideo.utils.HashUtils.hashPassword;
import static com.killrvideo.utils.HashUtils.isPasswordValid;
import static org.junit.jupiter.api.Assertions.*;

class HashUtilsTest {

    @Test
    public void testIsPasswordValidWithEmptyRealPasswd() {
        assertFalse(isPasswordValid("", "hash"));
    }

    @Test
    public void testIsPasswordValidWithEmptyHash() {
        assertFalse(isPasswordValid("passwd", ""));
    }

    @Test
    public void testIsPasswordValidWithMismatchPassword() {
        assertFalse(isPasswordValid("passwd", "hash"));
    }

    @Test
    public void testIsPasswordValid() {
        String realPassword = "passwd";
        String hash = hashPassword(realPassword);
        assertTrue(isPasswordValid(realPassword, hash));
    }
}
