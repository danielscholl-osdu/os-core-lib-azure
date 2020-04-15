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
import lombok.AllArgsConstructor;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.search.ClusterSettings;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.IElasticRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of {@link IElasticRepository} for azure.
 */
@AllArgsConstructor
@Component
@Lazy
public class ElasticCredentialRepository implements IElasticRepository {

    /**
     * KeyVault client.
     */
    @Inject
    private SecretClient secretClient;

    /**
     * OSDU application logger.
     */
    @Inject
    private JaxRsDpsLog log;

    /**
     * Returns Elasticsearch cluster settings. Note: multi-tenancy has been
     * de-scoped in R2, so the tenant is ignored for now.
     *
     * @param tenantInfo The tenant for which the credentials represent
     * @return Elasticsearch credentials
     */
    @Override
    public ClusterSettings getElasticClusterSettings(final TenantInfo tenantInfo) {
        URL esURL = getElasticURL();
        String username = getSecretWithValidation("elastic-username");
        String password = getSecretWithValidation("elastic-password");
        return buildSettings(esURL, username, password);
    }

    /**
     * Construct the cluster settings.
     * @param esURL URL for ES cluster
     * @param username Username for ES cluster
     * @param password Password for ES cluster
     * @return {@link ClusterSettings} representing the cluster
     */
    private ClusterSettings buildSettings(
            final URL esURL,
            final String username,
            final String password) {
        failIfNotHTTPS(esURL);
        return ClusterSettings.builder()
                .host(esURL.getHost())
                .port(esURL.getPort())
                .userNameAndPassword(String.format("%s:%s", username, password))
                .https(true)
                .tls(true)
                .build();
    }

    /**
     * Fail if the URL is not HTTPS.
     * @param u A URL
     */
    private void failIfNotHTTPS(final URL u) {
        if (!u.getProtocol().toLowerCase().equals("https")) {
            String error = String.format(
                    "Failing to initialize Elasticsearch settings for cluster with endpoint %s."
                            + "Traffic is not using HTTPS, which is insecure.", u);
            log.warning(error);
            throw new IllegalStateException(error);
        }
    }

    /**
     * @return fully parsed {@link URL} pointing to the Elasticsearch cluster.
     */
    private URL getElasticURL() {
        String qualifiedEndpoint = getSecretWithValidation("elastic-endpoint");
        try {
            return new URL(qualifiedEndpoint);
        } catch (MalformedURLException e) {
            String error = "Cannot parse Elasticsearch endpoint from KeyVault";
            log.warning(error, e);
            throw new IllegalStateException(error, e);
        }
    }

    /**
     * Gets a secret from KV and validates that it is not null or empty.
     * @param secretName name of secret
     * @return Secret value. This is guaranteed to be not null or empty.
     */
    private String getSecretWithValidation(final String secretName) {
        KeyVaultSecret secret = secretClient.getSecret(secretName);
        Validators.checkNotNull(secret, "Secret with name " + secretName);

        String secretValue = secret.getValue();
        Validators.checkNotNullAndNotEmpty(secretValue, "Secret Value for Secret with name " + secretName);

        return secretValue;
    }
}
