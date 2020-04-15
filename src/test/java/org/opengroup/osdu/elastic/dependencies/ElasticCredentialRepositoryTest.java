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

package org.opengroup.osdu.elastic.dependencies;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.search.ClusterSettings;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElasticCredentialRepositoryTest {
    @Mock
    private SecretClient secretClient;

    @Mock
    private JaxRsDpsLog log;

    private TenantInfo tenant = new TenantInfo();

    @InjectMocks
    private ElasticCredentialRepository repo;

    @Test
    void getClusterSettings_checksForValidURL_andLogsIfFails() {
        mockSecretValue("elastic-endpoint", "not-a-url");
        assertThrows(IllegalStateException.class, () -> repo.getElasticClusterSettings(tenant));
        verify(log, times(1)).warning(any(String.class), any(MalformedURLException.class));
    }

    @Test
    void getClusterSettings_checksForHTTPS_andLogsIfFails() {
        mockSecretValue("elastic-endpoint", "http://es-endpoint.com:80");
        mockSecretValue("elastic-username", "es-user");
        mockSecretValue("elastic-password", "es-pass");

        assertThrows(IllegalStateException.class, () -> repo.getElasticClusterSettings(tenant));
        verify(log, times(1)).warning(any(String.class));
    }

    @Test
    void getClusterSettings_checksForNullEndpoint() {
        mockSecretValue("elastic-endpoint", null);
        assertThrows(NullPointerException.class, () -> repo.getElasticClusterSettings(tenant));
    }

    @Test
    void getClusterSettings_checksForNullUsername() {
        mockSecretValue("elastic-endpoint", "https://es-endpoint.com:443");
        mockSecretValue("elastic-username", null);
        assertThrows(NullPointerException.class, () -> repo.getElasticClusterSettings(tenant));
    }

    @Test
    void getClusterSettings_checksForNullPassword() {
        mockSecretValue("elastic-endpoint", "https://es-endpoint.com:443");
        mockSecretValue("elastic-username", "es-user");
        mockSecretValue("elastic-password", null);
        assertThrows(NullPointerException.class, () -> repo.getElasticClusterSettings(tenant));
    }

    @Test
    void getClusterSettings_assemblesFromSecretsProperly() {
        mockSecretValue("elastic-endpoint", "https://es-endpoint.com:443");
        mockSecretValue("elastic-username", "es-user");
        mockSecretValue("elastic-password", "es-pass");

        ClusterSettings settings = repo.getElasticClusterSettings(tenant);
        assertTrue(settings.isHttps());
        assertTrue(settings.isHttps());
        assertEquals(443, settings.getPort());
        assertEquals("es-endpoint.com", settings.getHost());
    }

    void mockSecretValue(String secretName, String secretValue) {
        KeyVaultSecret secret = mock(KeyVaultSecret.class);
        doReturn(secret).when(secretClient).getSecret(secretName);
        doReturn(secretValue).when(secret).getValue();
    }
}
