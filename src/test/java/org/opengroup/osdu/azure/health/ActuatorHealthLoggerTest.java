package org.opengroup.osdu.azure.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.opengroup.osdu.azure.logging.CoreLogger;
import org.opengroup.osdu.azure.logging.CoreLoggerFactory;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.http.ApiVersion;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.boot.actuate.health.Status;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for LoggingHealthEndPointWebExtension.
 */
@ExtendWith(MockitoExtension.class)
public class ActuatorHealthLoggerTest {

    private static ActuatorHealthLogger loggingHealthEndpointWebExtension;
    private static HealthContributorRegistry registry;
    private static HealthEndpointGroups groups;

    @Mock
    private CoreLoggerFactory coreLoggerFactory;

    @Mock
    private CoreLogger logger;

    /**
     * Workaround for inability to mock static methods like getInstance().
     *
     * @param mock CoreLoggerFactory mock instance
     */
    private void mockSingleton(CoreLoggerFactory mock) {
        try {
            Field instance = CoreLoggerFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, mock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setup() {

        registry = mock(HealthContributorRegistry.class);
        groups = mock(HealthEndpointGroups.class);
    }

    @Mock
    SecurityContext securityContext;

    @Mock
    Health health;

    @Mock
    CompositeHealth compositeHealth;

    @Test
    public void healthTest_whenStatusIs_DOWN() {
        String[] path = new String[0];
        mockSingleton(coreLoggerFactory);
        when(coreLoggerFactory.getLogger(anyString())).thenReturn(logger);
        Mockito.when(health.getStatus()).thenReturn(Status.DOWN);

        Map<String, HealthComponent> map = new HashMap<>();
        map.put("keyvault", health);

        Mockito.when(compositeHealth.getComponents()).thenReturn(map);
        WebEndpointResponse<HealthComponent> expected = new WebEndpointResponse<>(compositeHealth, WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE);
        loggingHealthEndpointWebExtension = new ActuatorHealthLogger(registry, groups) {
            @Override
            protected WebEndpointResponse<HealthComponent> superClassCall(ApiVersion apiVersion, SecurityContext securityContext,
                                                                          boolean showAll, String... path) {
                return expected;
            }
        };


        WebEndpointResponse<HealthComponent> actual = loggingHealthEndpointWebExtension.health(ApiVersion.LATEST, securityContext, true, path);

        verify(logger, times(1)).error(anyString(), any(), any());

    }

    @Test
    public void healthTest_whenStatusIs_UP() {
        String[] path = new String[0];
        Mockito.when(health.getStatus()).thenReturn(Status.UP);
        Map<String, HealthComponent> map = new HashMap<>();
        map.put("keyvault", health);
        Mockito.when(compositeHealth.getComponents()).thenReturn(map);
        WebEndpointResponse<HealthComponent> expected = new WebEndpointResponse<>(compositeHealth, WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE);
        loggingHealthEndpointWebExtension = new ActuatorHealthLogger(registry, groups) {
            @Override
            protected WebEndpointResponse<HealthComponent> superClassCall(ApiVersion apiVersion, SecurityContext securityContext,
                                                                          boolean showAll, String... path) {
                return expected;
            }
        };

        WebEndpointResponse<HealthComponent> actual = loggingHealthEndpointWebExtension.health(ApiVersion.LATEST, securityContext, true, path);
        verify(logger, times(0)).error(anyString(), any(), any());

    }

}
