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
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;

import org.opengroup.osdu.core.common.model.http.AppException;

/**
 *  Class to generate the AAD authentication tokens.
 */
public final class AzureServicePrincipal {

    private static final int ERROR_STATUS_CODE = 500;
    private static final String ERROR_REASON = "Received empty token";
    private static final String ERROR_MESSAGE_SPN = "SPN client returned null token";
    private static final String ERROR_MESSAGE_MSI = "MSI client returned null token";

    /**
     * @param sp_id             AZURE CLIENT ID
     * @param sp_secret         AZURE CLIENT SECRET
     * @param tenant_id         AZURE TENANT ID
     * @param app_resource_id   AZURE APP RESOURCE ID
     * @return                  AUTHENTICATION TOKEN
     */
    public String getIdToken(final String sp_id, final String sp_secret, final String tenant_id, final String app_resource_id) {
        System.out.println("Access token token_msi is starting to build - abhiramTesting");

        IdentityClientBuilder identityClientBuilder = createIdentityClientBuilder();
        IdentityClient identityClientSPN = identityClientBuilder.tenantId(tenant_id)
                .clientId(sp_id)
                .clientSecret(sp_secret)
                .build();
        System.out.println("identityClientSPN created");

        TokenRequestContext requestContextSPN = new TokenRequestContext();
        String scope = String.format(app_resource_id, "/.default");
        requestContextSPN.addScopes(scope);
        AccessToken tokenSpnCreds = identityClientSPN.authenticateWithConfidentialClient(requestContextSPN).block();
        System.out.println("Access token token_spn_creds generated - abhiramTesting");

        if (tokenSpnCreds != null && tokenSpnCreds.getToken() != null) {
            System.out.println("Access token token_spn_creds is not null - abhiramTesting");
            System.out.println(tokenSpnCreds.getToken());
            return tokenSpnCreds.getToken();
        }
        throw new AppException(ERROR_STATUS_CODE, ERROR_REASON, ERROR_MESSAGE_SPN);
    }

    /**
     * Method to generate MSI tokens.
     * @return AUTHENTICATION TOKEN
     */
    public String getMSIToken() {
        System.out.println("Access token token_msi is starting to build - abhiramTesting");

        IdentityClientBuilder identityClientBuilder = createIdentityClientBuilder();
        IdentityClient identityClientMSI = identityClientBuilder.build();
        System.out.println("identityClientMSI created");

        TokenRequestContext requestContextMSI = new TokenRequestContext();
        requestContextMSI.addScopes("https://management.azure.com/");
        AccessToken tokenMsi =  identityClientMSI.authenticateToIMDSEndpoint(requestContextMSI).block();
        System.out.println("Access token token_msi generated - abhiramTesting");

        if (tokenMsi != null && tokenMsi.getToken() != null) {
            System.out.println("Access token token_msi is not null - abhiramTesting\n");
            System.out.println(tokenMsi.getToken());
            return tokenMsi.getToken();
        }
        throw new AppException(ERROR_STATUS_CODE, ERROR_REASON, ERROR_MESSAGE_MSI);
    }

    /**
     * @return IdentityClientBuilder
     */
    IdentityClientBuilder createIdentityClientBuilder() {
        return new IdentityClientBuilder();
    }

}
