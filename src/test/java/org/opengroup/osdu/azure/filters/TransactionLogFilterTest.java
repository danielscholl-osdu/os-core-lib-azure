package org.opengroup.osdu.azure.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.logging.AzureLogger;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TransactionLogFilter}
 */
@ExtendWith(MockitoExtension.class)
public class TransactionLogFilterTest {
    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private HttpServletResponse servletResponse;
    @Mock
    private FilterChain filterChain;
    @Mock
    private AzureLogger azureLogger;

    @InjectMocks
    private TransactionLogFilter logFilter;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void testStartAndEndMessagesAreLoggedProperly() throws Exception {
        final String startLogMessage = "Start Web-API PUT records Headers: {correlation-id:abc}";
        final String endMessage = "End Web-API PUT records Headers: {correlation-id:abc} timeTaken:";
        when(servletRequest.getMethod()).thenReturn("PUT");
        when(servletRequest.getServletPath()).thenReturn("records");
        when(servletRequest.getHeader(eq(DpsHeaders.CORRELATION_ID))).thenReturn("abc");
        when(servletResponse.getHeader(eq(DpsHeaders.CORRELATION_ID))).thenReturn("abc");
        final ArgumentCaptor<String> logMessageCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(azureLogger).info(eq("TxnLogger"), logMessageCaptor.capture());
        this.logFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(servletRequest, times(2)).getMethod();
        verify(servletRequest, times(2)).getServletPath();
        verify(servletRequest, times(2)).getHeader(eq(DpsHeaders.CORRELATION_ID));
        verify(servletResponse, times(2)).getHeader(eq(DpsHeaders.CORRELATION_ID));
        assertEquals(2, logMessageCaptor.getAllValues().size());
        assertEquals(startLogMessage, logMessageCaptor.getAllValues().get(0));
        assertEquals(true, logMessageCaptor.getAllValues().get(1).startsWith(endMessage));
    }

    @Test
    public void testStartAndEndMessagesAreLoggedProperlyWithNoHeaders() throws Exception {
        final String startLogMessage = "Start Web-API PUT records Headers: {}";
        final String endMessage = "End Web-API PUT records Headers: {} timeTaken:";
        when(servletRequest.getMethod()).thenReturn("PUT");
        when(servletRequest.getServletPath()).thenReturn("records");
        final ArgumentCaptor<String> logMessageCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(azureLogger).info(eq("TxnLogger"), logMessageCaptor.capture());
        this.logFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(servletRequest, times(2)).getMethod();
        verify(servletRequest, times(2)).getServletPath();
        assertEquals(2, logMessageCaptor.getAllValues().size());
        assertEquals(startLogMessage, logMessageCaptor.getAllValues().get(0));
        assertEquals(true, logMessageCaptor.getAllValues().get(1).startsWith(endMessage));
    }
}
