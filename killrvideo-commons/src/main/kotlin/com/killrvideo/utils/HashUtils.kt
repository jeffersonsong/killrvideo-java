package com.killrvideo.utils

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils.isNotBlank

/**
 * Working with Hasehed passwords.
 *
 * @author DataStax Developer Advocates team.
 */
object HashUtils {
    /**
     * Work with passwords.
     *
     * @param password
     * current password
     * @return
     * passwortd hashed
     */
    fun hashPassword(password: String): String =
        String(DigestUtils.getSha512Digest().digest(password.toByteArray()))

    /**
     * ATest password againast hashed version.
     *
     * @param realPassword
     * clear text password
     * @param hash
     * hash to evaluate
     * @return
     * if vpaqssword is valid.
     */
    fun isPasswordValid(realPassword: String, hash: String): Boolean =
        isNotBlank(realPassword) && isNotBlank(hash) &&
                hashPassword(realPassword.trim { it <= ' ' }).compareTo(hash) == 0
}