package org.opengroup.osdu.azure.util;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
import org.opengroup.osdu.core.common.model.http.AppException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AzureServicePrincipalTest {

    private static final String accessTokenContent = "some-access-token";
    private static final String spId = "client-id";
    private static final String spSecret = "client-secret";
    private static final String tenantId = "tenant-id";
    private static final String appResourceId = "app-resource-id";

    @Mock
    DefaultAzureCredentialBuilder credentialBuilder;

    @Mock
    DefaultAzureCredential credential;

    @Spy
    private AzureServicePrincipal azureServicePrincipal;

    @Test
    public void TestGetToken() throws Exception {
        AccessToken accessToken = new AccessToken(accessTokenContent, OffsetDateTime.now());

        when(azureServicePrincipal.getDefaultAzureCredential()).thenReturn(credential);
        when(credential.getToken(any(TokenRequestContext.class))).thenReturn(Mono.just(accessToken));

        String result = azureServicePrincipal.getToken(appResourceId);
        assertEquals(accessTokenContent, result);

        verify(credential, times(1)).getToken(any(TokenRequestContext.class));
    }

    @Test
    public void TestGetToken_failure() throws Exception {
        when(azureServicePrincipal.getDefaultAzureCredential()).thenReturn(credential);
        when(credential.getToken(any(TokenRequestContext.class))).thenReturn(Mono.empty());

        assertThrows(AppException.class, () -> azureServicePrincipal.getToken(appResourceId));

        verify(credential, times(1)).getToken(any(TokenRequestContext.class));
    }

    /**
     *  This test is added for end to verification whether tokens are getting generated.
     */
    @Disabled
    @Test
    public void VerifyingEndToEndScenario() throws Exception {
        String result = new AzureServicePrincipal().getToken(appResourceId);
        assertNotNull(result);
    }
}