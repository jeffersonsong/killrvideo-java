package com.killrvideo.messaging.dao

import com.google.common.eventbus.Subscribe
import killrvideo.common.CommonEvents.ErrorEvent
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.PrintWriter
import javax.annotation.PreDestroy

/**
 * Catch exceptions and create a [CassandraMutationError] in EventBus.
 *
 * @author DataStax Developer Advocates team.
 */
@Component
class ErrorProcessor(private val errorLogFile: PrintWriter) {
    private val logger = KotlinLogging.logger {  }

    @Autowired
    constructor(@Value("\${killrvideo.cassandra.mutation-error-log: /tmp/killrvideo-mutation-errors.log}") mutationErrorLog: String?) : this(
        PrintWriter(mutationErrorLog)
    )

    /**
     * Here we just record the original Grpc request so that we can replay
     * them later.
     *
     * An alternative impl can just push the request to a message queue or
     * event bus so that it can be handled by another micro-service
     *
     */
    @Subscribe
    fun handle(errorEvent: ErrorEvent) {
        val msg = String.format("Recording mutation error %s", errorEvent.errorMessage + errorEvent.errorStack)
        logger.error(msg)
        errorLogFile.append(msg).append("\n***********************\n")
        errorLogFile.flush()
    }

    /**
     * Closing log file.
     */
    @PreDestroy
    fun closeErrorLogFile() {
        errorLogFile.close()
    }
}
