package com.killrvideo.messaging.dao

import com.killrvideo.messaging.utils.MessagingUtils.mapCustomError
import com.killrvideo.messaging.utils.MessagingUtils.mapError
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Interface to work with Events.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
interface MessagingDao {
    /**
     * Will send an event to target destination.
     *
     * @param targetDestination
     * adress of destination : queue, topic, shared memory (className).
     * @param event
     * event serialized as binary
     */
    fun sendEvent(targetDestination: String, event: Any): CompletableFuture<*>

    /**
     * Channel to send errors.
     */
    val errorDestination: String

    /**
     * Send errors to bus.
     *
     * @param serviceName
     * @param t
     */
    fun sendErrorEvent(serviceName: String, t: Throwable): CompletableFuture<*> =
        sendEvent(errorDestination, mapError(t))

    /**
     * Send error event to bus.
     */
    fun sendErrorEvent(serviceName: String, customError: String): Future<*> =
        sendEvent(errorDestination, mapCustomError(customError))
}
