package com.killrvideo.messaging.dao;

import killrvideo.common.CommonEvents.ErrorEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static com.killrvideo.messaging.utils.MessagingUtils.mapError;
import static org.mockito.Mockito.*;

class ErrorProcessorTest {
    private ErrorProcessor processor;
    private PrintWriter printWriter;

    @BeforeEach
    public void setUp() {
        this.printWriter = mock(PrintWriter.class);
        when(this.printWriter.append(anyString())).thenReturn(this.printWriter);
        this.processor = new ErrorProcessor(printWriter);
    }

    @Test
    public void testHandle() {
        Throwable t = new RuntimeException("Something bad happened.");
        ErrorEvent event = mapError(t);

        this.processor.handle(event);
        this.processor.closeErrorLogFile();

        verify(this.printWriter, times(2)).append(anyString());
        verify(this.printWriter, times(1)).flush();
        verify(this.printWriter, times(1)).close();
    }
}