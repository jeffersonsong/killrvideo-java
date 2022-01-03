package com.killrvideo.utils

import mu.KotlinLogging
import java.net.Socket

/**
 * Utilities for network.
 *
 * @author DataStax Developer Advocates Team
 */
object IOUtils {
    /** Logger for Graph.  */
    private val logger = KotlinLogging.logger {  }

    /**
     * Try to open socket on target Adress and port to evaluate if service is running.
     *
     * @param service
     * service name (label)
     * @param address
     * target adress
     * @param port
     * target port
     * @return
     * if something is listening on target URL
     */
    fun isServiceReachableAndListening(service: String?, address: String?, port: Int): Boolean {
        try {
            Socket(address, port).use { s ->
                s.reuseAddress = true
                logger.info("Connection to {}:{} is working for service {}", address, port, service)
                return true
            }
        } catch (e: Exception) {
            logger.error("Cannot connect to service {} on {}:{} is working for service {}", service, address, port, e)
            return false
        }
    }
}