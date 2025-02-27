package org.opengroup.osdu.azure.logging;

import com.microsoft.applicationinsights.TelemetryClient;

import com.microsoft.applicationinsights.telemetry.RemoteDependencyTelemetry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ApplicationInsightsLoggerTest {

    private static TelemetryClient telemetryClient;

    @Test
    public void testLogDependency() {
        telemetryClient = mock(TelemetryClient.class);
        MockedStatic<ApplicationInsightsClient> applicationInsightsClientMockedStatic = Mockito.mockStatic(ApplicationInsightsClient.class);
        applicationInsightsClientMockedStatic.when(ApplicationInsightsClient::getTelemetryClient).thenReturn(telemetryClient);
        ApplicationInsightsLogger applicationInsightsLogger = new ApplicationInsightsLogger(LoggerFactory.getLogger("TEST_LOGGER"));
        final ArgumentCaptor<RemoteDependencyTelemetry> telemetryCaptor = ArgumentCaptor.forClass(RemoteDependencyTelemetry.class);
        doNothing().when(telemetryClient).trackDependency(telemetryCaptor.capture());
        DependencyPayload payload = new DependencyPayload("DependencyName", "Dependency/Command/Name", Duration.ofMillis(1000), "200", true);
        applicationInsightsLogger.logDependency(payload);
        assertEquals(1, telemetryCaptor.getAllValues().size());
        assertEquals("DependencyName", telemetryCaptor.getAllValues().get(0).getName());
        assertEquals("Dependency/Command/Name", telemetryCaptor.getAllValues().get(0).getCommandName());
        assertEquals("HTTP", telemetryCaptor.getAllValues().get(0).getType());
        assertEquals("Dependency/Command/Name", telemetryCaptor.getAllValues().get(0).getTarget());
        assertEquals(1000, telemetryCaptor.getAllValues().get(0).getDuration().getTotalMilliseconds());
        assertEquals("200", telemetryCaptor.getAllValues().get(0).getResultCode());
        assertEquals(true, telemetryCaptor.getAllValues().get(0).getSuccess());
    }
}
