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

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.search.IdToken;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.Map;


/**
 * Azure Service Principle token service.
 */
@Component
@Lazy
public class AzureServicePrincipleTokenService {

    @Inject
    @Named("AAD_OBO_API")
    private String aadClientId;
    @Inject
    @Named("APP_DEV_SP_USERNAME")
    private String clientID;
    @Inject
    @Named("APP_DEV_SP_PASSWORD")
    private String clientSecret;
    @Inject
    @Named("APP_DEV_SP_TENANT_ID")
    private String tenantId;

    private final AzureServicePrincipal azureServicePrincipal = new AzureServicePrincipal();

    private Map<String, Object> tokenCache = new HashMap<>();

    /**
     * @return service principle's authorization token
     */
    public String getAuthorizationToken() {
        String accessToken = "";
        try {
            IdToken cachedToken = (IdToken) this.tokenCache.get("token");
            if (!IdToken.refreshToken(cachedToken)) {
                return cachedToken.getTokenValue();
            }

            // Use DefaultAzureCredential which will automatically handle all authentication scenarios
            accessToken = this.azureServicePrincipal.getToken(aadClientId + "/.default");

            IdToken idToken = IdToken.builder()
                    .tokenValue(accessToken)
                    .expirationTimeMillis(JWT.decode(accessToken).getExpiresAt().getTime())
                    .build();
            this.tokenCache.put("token", idToken);
        } catch (JWTDecodeException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Persistence error", "Invalid token, error decoding", e);
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Persistence error", "Error generating token", e);
        }

        return accessToken;
    }
}
