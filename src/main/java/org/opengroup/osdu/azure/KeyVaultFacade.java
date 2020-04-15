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
import org.opengroup.osdu.common.Validators;

import java.util.logging.Logger;

/**
 * A simpler interface for interacting with keyVault.
 *
 * Usage Examples:
 * <pre>
 * {@code
 *     @Bean
 *     @Named("MY_STRING_SECRET")
 *     public String cosmosKey(SecretClient kv) {
 *         return KeyVaultFacade.getSecretWithValidation(kv, "my-secret-name");
 *     }
 * }
 * </pre>
 */
public final class KeyVaultFacade {

    private static final Logger LOGGER = Logger.getLogger(KeyVaultFacade.class.getName());

    /**
     * Private constructor -- this class should never be instantiated.
     */
    private KeyVaultFacade() {
    }

    /**
     * @param kv         Client configured to the correct vault
     * @param secretName Name of secret to return
     * @return A guaranteed to be non null secret value
     */
    public static String getSecretWithValidation(final SecretClient kv, final String secretName) {
        KeyVaultSecret secret = kv.getSecret(secretName);
        Validators.checkNotNull(secret, secretName);
        String secretValue = secret.getValue();
        Validators.checkNotNullAndNotEmpty(secretValue, secretName);
        return secretValue;
    }
}
