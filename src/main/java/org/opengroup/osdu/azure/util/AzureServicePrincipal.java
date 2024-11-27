// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.azure.util;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import org.opengroup.osdu.core.common.model.http.AppException;

/**
 * Class to generate the AAD authentication tokens using DefaultAzureCredential.
 *
 * DefaultAzureCredential tries the following authentication methods in order:
 * 1. Environment Credentials (service principal credentials in environment variables)
 * 2. Workload Identity Credentials (when running in AKS with Workload Identity)
 * 3. Managed Identity Credentials (including Pod Identity)
 * 4. Azure CLI Credentials (for local development)
 * 5. Visual Studio Code Credentials (for local development)
 * 6. Azure PowerShell Credentials (for local development)
 * 7. Interactive Browser Credentials (for local development)
 *
 * For production deployments, configure one of:
 * - Workload Identity (recommended for AKS)
 * - Managed Identity/Pod Identity (legacy approach for AKS)
 * - Environment Variables (for non-AKS scenarios)
 */
public final class AzureServicePrincipal {

    private static final int ERROR_STATUS_CODE = 500;
    private static final String ERROR_REASON = "Received empty token";

    /**
     * Gets a token using DefaultAzureCredential which supports multiple authentication methods.
     * @param scopes The scopes to request the token for
     * @return Authentication token
     */
    public String getToken(final String... scopes) {
        TokenRequestContext request = new TokenRequestContext();
        request.addScopes(scopes);

        DefaultAzureCredential credential = getDefaultAzureCredential();
        AccessToken token = credential.getToken(request).block();

        if (token != null && token.getToken() != null) {
            return token.getToken();
        }
        throw new AppException(ERROR_STATUS_CODE, ERROR_REASON, "Failed to obtain token from DefaultAzureCredential");
    }

    /**
     * @param sp_id             AZURE CLIENT ID
     * @param sp_secret         AZURE CLIENT SECRET
     * @param tenant_id         AZURE TENANT ID
     * @param app_resource_id   AZURE APP RESOURCE ID
     * @return                  AUTHENTICATION TOKEN
     */
    public String getIdToken(final String sp_id, final String sp_secret, final String tenant_id, final String app_resource_id) {
        return getToken(app_resource_id.concat("/.default"));
    }

    /**
     * Method to generate MSI tokens.
     * @return AUTHENTICATION TOKEN
     */
    public String getMSIToken() {
        return getToken("https://management.azure.com/.default");
    }

    /**
     * Creates and returns a new DefaultAzureCredential instance.
     * @return DefaultAzureCredential configured with default settings
     */
    protected DefaultAzureCredential getDefaultAzureCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }
}
