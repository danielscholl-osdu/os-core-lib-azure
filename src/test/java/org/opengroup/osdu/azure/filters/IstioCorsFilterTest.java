package org.opengroup.osdu.azure.filters;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.FilterChain;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link IstioCorsFilter}
 */
public class IstioCorsFilterTest {

    /**
     * Tests for a valid CORS and Preflight request.
     */
    @Test
    public void testdoFilter_Success() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "https://www.apache.org");
        request.setMethod("OPTIONS");
        request.addHeader("Access-Control-Request-Method", "OPTIONS");

        IstioCorsFilter customCorsFilter = new IstioCorsFilter();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        customCorsFilter.doFilter(request, response, filterChain);

        Assert.assertTrue(response.getHeader("Access-Control-Allow-Origin")==null);
        Assert.assertTrue(response.getHeader("Access-Control-Allow-Methods")== null);
        Assert.assertTrue(response.getHeader("Access-Control-Max-Age")==null);
        Assert.assertTrue(response.getHeader("Access-Control-Allow-Headers")==null);
        Assert.assertTrue(response.getHeader("Access-Control-Expose-Headers")==null);
        Assert.assertTrue(response.getStatus()==200);
    }

    /**
     * Tests for a non-CORS request.
     */
    @ExtendWith(MockitoExtension.class)
    @Test
    public void testdoFilter_NonCORSRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader("Origin", "https://www.apache.org");
        request.setMethod("GET");
        request.addHeader("Access-Control-Request-Method", "GET");
        IstioCorsFilter customCorsFilter = new IstioCorsFilter();

        customCorsFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Tests if a GET request is treated as simple request; Not a Preflight request.
     */
    @Test
    public void testisPreFlightRequest_SimpleGET() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        request.setMethod("GET");
        request.addHeader("Origin", "https://www.apache.org");

        IstioCorsFilter customCorsFilter = new IstioCorsFilter();

        customCorsFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Tests if request origin is null; It is not a Preflight request.
     */
    @Test
    public void testisPreFlightRequest_OriginNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        request.setMethod("OPTIONS");

        IstioCorsFilter customCorsFilter = new IstioCorsFilter();

        customCorsFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Tests if request is valid or not for invalid origin (origin = null)
     */
    @Test
    public void testisCorsRequest_OriginNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        request.setMethod("OPTIONS");

        IstioCorsFilter customCorsFilter = new IstioCorsFilter();

        customCorsFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Tests if request is valid or not for a valid origin
     */
    @Test
    public void testisCorsRequest_ValidOrigin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        request.addHeader("Origin", "https://www.apache.org");
        request.setMethod("OPTIONS");

        IstioCorsFilter customCorsFilter = new IstioCorsFilter();

        customCorsFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }


}
