package com.killrvideo.messaging.dao;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

import killrvideo.common.CommonEvents.ErrorEvent;

/**
 * Catch exceptions and create a {@link CassandraMutationError} in EventBus.
 *
 * @author DataStax Developer Advocates team.
 */
@Component
public class ErrorProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorProcessor.class);

    private final PrintWriter errorLogFile;

    @Autowired
    public ErrorProcessor(@Value("${killrvideo.cassandra.mutation-error-log: /tmp/killrvideo-mutation-errors.log}")
                                  String mutationErrorLog) throws FileNotFoundException {
        this(new PrintWriter(mutationErrorLog));
    }

    public ErrorProcessor(PrintWriter errorLogFile) {
        this.errorLogFile = errorLogFile;
    }

    /**
     * Here we just record the original Grpc request so that we can replay
     * them later.
     *
     * An alternative impl can just push the request to a message queue or
     * event bus so that it can be handled by another micro-service
     *
     */
    @Subscribe
    public void handle(final ErrorEvent errorEvent) {
        String msg = String.format("Recording mutation error %s", errorEvent.getErrorMessage() + errorEvent.getErrorStack());
        LOGGER.error(msg);
        errorLogFile.append(msg).append("\n***********************\n");
        errorLogFile.flush();
    }

    /**
     * Closing log file.
     */
    @PreDestroy
    public void closeErrorLogFile() {
        this.errorLogFile.close();
    }
}
