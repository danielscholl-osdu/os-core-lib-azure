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

package org.opengroup.osdu.azure;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class KeyVaultFacadeTest {
    @Mock
    private SecretClient kv;

    @Test
    void getSecretWithValidation_checksForNullResponse() {
        // the null response is the default behavior, but its included here to be explicit
        doReturn(null).when(kv).getSecret("secret-name");
        assertThrows(NullPointerException.class, () -> KeyVaultFacade.getSecretWithValidation(kv, "secret-name"));
    }


    @Test
    void getSecretWithValidation_checksForNullValueWithinResponse() {
        KeyVaultSecret secret = mock(KeyVaultSecret.class);
        // the null response is the default behavior, but its included here
        // to be explicit
        doReturn(null).when(secret).getValue();
        doReturn(secret).when(kv).getSecret("secret-name");

        assertThrows(NullPointerException.class, () -> KeyVaultFacade.getSecretWithValidation(kv, "secret-name"));
    }

    @Test
    void getSecretWithValidation_returnsCorrectSecret() {
        KeyVaultSecret secret = mock(KeyVaultSecret.class);
        doReturn("secret-value").when(secret).getValue();
        doReturn(secret).when(kv).getSecret("secret-name");

        String secretValue = KeyVaultFacade.getSecretWithValidation(kv, "secret-name");
        assertEquals("secret-value", secretValue);
    }
}
