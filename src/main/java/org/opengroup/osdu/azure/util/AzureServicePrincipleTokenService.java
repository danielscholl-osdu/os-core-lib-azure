package org.opengroup.osdu.azure.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.search.IdToken;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
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
            accessToken = this.azureServicePrincipal.getIdToken(clientID, clientSecret, tenantId, aadClientId);
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
