package com.killrvideo.utils;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for network.
 *
 * @author DataStax Developer Advocates Team
 */
public class IOUtils {
    
    /** Logger for Graph. */
    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);
    
    /**
     * Hide constructor (Utility class).
     */
    private IOUtils() {}
    
    /**
     * Try to open socket on target Adress and port to evaluate if service is running.
     * 
     * @param service
     *      service name (label)
     * @param address
     *      target adress
     * @param port
     *      target port
     * @return
     *      if something is listening on target URL
     */
    public static boolean isServiceReachableAndListening(String service, String address, int port) {
        try (Socket s = new Socket(address, port)) {
            s.setReuseAddress(true);
            LOGGER.info("Connection to {}:{} is working for service {}", address, port, service);
            return true;
        } catch (Exception e) {
            LOGGER.error("Cannot connect to service {} on {}:{} is working for service {}", service, address, port, e);
            return false;
        }
    }

}
