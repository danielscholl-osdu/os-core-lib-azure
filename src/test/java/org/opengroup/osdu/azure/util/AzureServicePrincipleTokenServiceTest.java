package org.opengroup.osdu.azure.util;

import com.azure.security.keyvault.secrets.SecretClient;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opengroup.osdu.azure.di.AzureActiveDirectoryConfiguration;
import org.opengroup.osdu.azure.di.PodIdentityConfiguration;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.search.IdToken;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.opengroup.osdu.azure.util.AuthUtils.getClientSecret;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({IdToken.class})
public class AzureServicePrincipleTokenServiceTest {

    private static final String clientSecret = "client-secret";
    private static final String accessToken = "accessToken";
    private static final String newClientSecret = "new-client-secret";

    @Mock
    private PodIdentityConfiguration podIdentityConfiguration;

    @Mock
    private Map<String, Object> tokenCache;

    @Mock
    private AzureServicePrincipleTokenService azureServicePrincipleTokenService;

    @Mock
    private AzureServicePrincipal azureServicePrincipal;

    @Mock
    private AzureActiveDirectoryConfiguration aadConfiguration;

    @Mock
    private SecretClient sc;


    @Before
    public void setup() {
    }

    @Test
    public void should_successfully_generate_authorization_token() throws UnsupportedEncodingException {
        mockStatic(IdToken.class);

        when((IdToken) tokenCache.get(any())).thenReturn(any());
        when(IdToken.refreshToken(any())).thenReturn(true);
        when(!podIdentityConfiguration.getIsEnabled()).thenReturn(false);
        when(this.azureServicePrincipal.getIdToken(any(), any(), any(), any())).thenReturn(accessToken);

        String result = azureServicePrincipleTokenService.getAuthorizationToken();
        assertEquals(accessToken, result);
    }

    @Test
    public void should_throws_appException() throws UnsupportedEncodingException {
        mockStatic(IdToken.class);

        when((IdToken) tokenCache.get(any())).thenReturn(any());
        when(IdToken.refreshToken(any())).thenReturn(true);
        when(!podIdentityConfiguration.getIsEnabled()).thenReturn(false);
        when(this.azureServicePrincipal.getIdToken(any(), any(), any(), any())).thenThrow(new AppException(HttpStatus.SC_NOT_FOUND, any(), any()));

        assertThrows(AppException.class, () -> azureServicePrincipleTokenService.getAuthorizationToken());
    }

    @Test
    public void should_successfully_get_new_client_secret() throws UnsupportedEncodingException {
        mockStatic(IdToken.class);

        when((IdToken) tokenCache.get(any())).thenReturn(any());
        when(IdToken.refreshToken(any())).thenReturn(true);
        when(!podIdentityConfiguration.getIsEnabled()).thenReturn(false);
        when(this.azureServicePrincipal.getIdToken(any(), any(), any(), any())).thenThrow(new AppException(HttpStatus.SC_UNAUTHORIZED, any(), any()));
        when(getClientSecret(aadConfiguration, sc)).thenReturn(newClientSecret);
        when(clientSecret.equals(newClientSecret)).thenReturn(false);
        when(this.azureServicePrincipal.getIdToken(any(), any(), any(), any())).thenReturn(accessToken);

        String result = azureServicePrincipleTokenService.getAuthorizationToken();
        assertEquals(accessToken, result);
    }

    @Test
    public void should_throws_appException_if_both_client_secret_equals() throws UnsupportedEncodingException {
        mockStatic(IdToken.class);

        when((IdToken) tokenCache.get(any())).thenReturn(any());
        when(IdToken.refreshToken(any())).thenReturn(true);
        when(!podIdentityConfiguration.getIsEnabled()).thenReturn(false);
        when(this.azureServicePrincipal.getIdToken(any(), any(), any(), any())).thenThrow(new AppException(HttpStatus.SC_UNAUTHORIZED, any(), any()));
        when(getClientSecret(aadConfiguration, sc)).thenReturn(newClientSecret);
        when(clientSecret.equals(newClientSecret)).thenReturn(true);

        assertThrows(AppException.class, () -> azureServicePrincipleTokenService.getAuthorizationToken());
    }
}