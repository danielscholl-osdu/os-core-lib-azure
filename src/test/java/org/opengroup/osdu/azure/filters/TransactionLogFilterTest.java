//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.azure.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
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
    private static final int STATUS_CODE = 200;
    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private HttpServletResponse servletResponse;
    @Mock
    private FilterChain filterChain;
    @Mock
    private JaxRsDpsLog jaxRsDpsLog;

    @InjectMocks
    private TransactionLogFilter logFilter;

    @BeforeEach
    public void setup() {
        when(servletResponse.getStatus()).thenReturn(STATUS_CODE);
    }

    @Test
    public void testStartAndEndMessagesAreLoggedProperly() throws Exception {
        final String startLogMessage = "Start Web-API PUT records Headers: {correlation-id:abc}";
        final String endMessage = "End Web-API PUT records Headers: {correlation-id:abc} status=200 time=";
        when(servletRequest.getMethod()).thenReturn("PUT");
        when(servletRequest.getServletPath()).thenReturn("records");
        when(servletRequest.getHeader(eq(DpsHeaders.CORRELATION_ID))).thenReturn("abc");
        when(servletResponse.getHeader(eq(DpsHeaders.CORRELATION_ID))).thenReturn("abc");
        final ArgumentCaptor<String> logMessageCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(jaxRsDpsLog).info(eq("TxnLogger"), logMessageCaptor.capture());
        this.logFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(servletRequest, times(2)).getMethod();
        verify(servletRequest, times(2)).getServletPath();
        verify(servletRequest, times(2)).getHeader(eq(DpsHeaders.CORRELATION_ID));
        verify(servletResponse, times(2)).getHeader(eq(DpsHeaders.CORRELATION_ID));
        verify(servletResponse, times(1)).getStatus();
        assertEquals(2, logMessageCaptor.getAllValues().size());
        assertEquals(startLogMessage, logMessageCaptor.getAllValues().get(0));
        assertEquals(true, logMessageCaptor.getAllValues().get(1).startsWith(endMessage));
    }

    @Test
    public void testStartAndEndMessagesAreLoggedProperlyWithNoHeaders() throws Exception {
        final String startLogMessage = "Start Web-API PUT records Headers: {}";
        final String endMessage = "End Web-API PUT records Headers: {} status=200 time=";
        when(servletRequest.getMethod()).thenReturn("PUT");
        when(servletRequest.getServletPath()).thenReturn("records");
        final ArgumentCaptor<String> logMessageCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(jaxRsDpsLog).info(eq("TxnLogger"), logMessageCaptor.capture());
        this.logFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(servletRequest, times(2)).getMethod();
        verify(servletRequest, times(2)).getServletPath();
        verify(servletResponse, times(1)).getStatus();
        assertEquals(2, logMessageCaptor.getAllValues().size());
        assertEquals(startLogMessage, logMessageCaptor.getAllValues().get(0));
        assertEquals(true, logMessageCaptor.getAllValues().get(1).startsWith(endMessage));
    }
}
