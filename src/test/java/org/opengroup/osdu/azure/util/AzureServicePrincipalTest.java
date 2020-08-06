package org.opengroup.osdu.azure.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AzureServicePrincipalTest {

    private static final String accessToken = "some-access-token";
    private static final String spId = "client-id";
    private static final String spSecret = "client-secret";
    private static final String tenantId = "tenant-id";
    private static final String appResourceId = "app-resource-id";

    @Mock
    private HttpClient httpClient;

    @Mock
    private Mono<HttpResponse> responseMono;

    @Mock
    private HttpResponse httpResponse;

    @Spy
    private AzureServicePrincipal azureServicePrincipal;

    @Test
    public void ShouldSuccessfullyGenerateToken() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("access_token", accessToken);

        Mono<String> contentMono = Mono.just(jsonObject.toString());

        when(azureServicePrincipal.createHttpClient()).thenReturn(httpClient);
        when(httpClient.send(any(HttpRequest.class))).thenReturn(responseMono);
        when(responseMono.block()).thenReturn(httpResponse);
        when(httpResponse.getBodyAsString()).thenReturn(contentMono);

        String result = azureServicePrincipal.getIdToken(spId, spSecret, tenantId, appResourceId);

        assertEquals(accessToken, result);
        verify(httpClient, times(1)).send(any(HttpRequest.class));
        verify(responseMono, times(1)).block();
        verify(httpResponse, times(1)).getBodyAsString();

    }

    /**
     *  This test is added for end to verification whether tokens are getting generated.
     */
    //
    @Disabled
    @Test
    public void VerifyingEndToEndScenario() throws Exception {

        String spId = "";
        String spSecret = "";
        String tenantId = "";
        String appResourceId = "";

        String result = new AzureServicePrincipal().getIdToken(spId, spSecret, tenantId, appResourceId);

    }
}
