package org.opengroup.osdu.azure.util;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AzureServicePrincipalTest {

    private static final String accessTokenContent = "some-access-token";
    private static final String spId = "client-id";
    private static final String spSecret = "client-secret";
    private static final String tenantId = "tenant-id";
    private static final String appResourceId = "app-resource-id";

    @Mock
    IdentityClientBuilder identityClientBuilder;

    @Mock
    IdentityClient identityClient;

    @Mock
    private Mono<AccessToken> responseMono;

    @Spy
    private AzureServicePrincipal azureServicePrincipal;

    @Test
    public void TestGenerateIDToken() throws Exception {

        AccessToken accessToken = new AccessToken(accessTokenContent, OffsetDateTime.now());

        when(azureServicePrincipal.createIdentityClientBuilder()).thenReturn(identityClientBuilder);

        when(identityClientBuilder.tenantId(any(String.class))).thenReturn(identityClientBuilder);
        when(identityClientBuilder.clientId(any(String.class))).thenReturn(identityClientBuilder);
        when(identityClientBuilder.clientSecret(any(String.class))).thenReturn(identityClientBuilder);
        when(identityClientBuilder.build()).thenReturn(identityClient);

        when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class))).thenReturn(responseMono);
        when(responseMono.block()).thenReturn(accessToken);

        String result = azureServicePrincipal.getIdToken(spId, spSecret, tenantId, appResourceId);
        assertEquals(accessTokenContent, result);
    }

    @Test
    public void TestGenerateIDToken_failure() throws Exception {

        AccessToken accessToken = new AccessToken(accessTokenContent, OffsetDateTime.now());

        when(azureServicePrincipal.createIdentityClientBuilder()).thenReturn(identityClientBuilder);

        when(identityClientBuilder.tenantId(any(String.class))).thenReturn(identityClientBuilder);
        when(identityClientBuilder.clientId(any(String.class))).thenReturn(identityClientBuilder);
        when(identityClientBuilder.clientSecret(any(String.class))).thenReturn(identityClientBuilder);
        when(identityClientBuilder.build()).thenReturn(identityClient);

        when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class))).thenReturn(responseMono);
        when(responseMono.block()).thenReturn(null);

        assertThrows(AppException.class, () -> azureServicePrincipal.getIdToken(spId, spSecret, tenantId, appResourceId));
    }

    @Test
    public void TestGenerateMsiToken() throws Exception {

        AccessToken accessToken = new AccessToken(accessTokenContent, OffsetDateTime.now());

        when(azureServicePrincipal.createIdentityClientBuilder()).thenReturn(identityClientBuilder);
        when(identityClientBuilder.build()).thenReturn(identityClient);
        when(identityClient.authenticateToIMDSEndpoint(any(TokenRequestContext.class))).thenReturn(responseMono);
        when(responseMono.block()).thenReturn(accessToken);

        String result = azureServicePrincipal.getMSIToken();
        assertEquals(accessTokenContent, result);
    }

    @Test
    public void TestGenerateMsiToken_failure() throws Exception {
        AccessToken accessToken = new AccessToken(accessTokenContent, OffsetDateTime.now());

        when(azureServicePrincipal.createIdentityClientBuilder()).thenReturn(identityClientBuilder);
        when(identityClientBuilder.build()).thenReturn(identityClient);
        when(identityClient.authenticateToIMDSEndpoint(any(TokenRequestContext.class))).thenReturn(responseMono);
        when(responseMono.block()).thenReturn(null);

        assertThrows(AppException.class, () -> azureServicePrincipal.getMSIToken());
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
