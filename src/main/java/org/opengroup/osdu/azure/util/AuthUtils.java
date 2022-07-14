package org.opengroup.osdu.azure.util;

import com.azure.security.keyvault.secrets.SecretClient;
import com.nimbusds.jwt.JWTClaimsSet;
import org.opengroup.osdu.azure.KeyVaultFacade;
import org.opengroup.osdu.azure.di.AzureActiveDirectoryConfiguration;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Base64;

/**
 * Utils for auth.
 */
@Component
public final class AuthUtils {

    private static final String SECRET_NAME= "app-dev-sp-password" ;

    /**
     * Get claims set from JWT token.
     *
     * @param jwtToken the JWT token string
     * @return the JWTClaimsSet object
     */
    public JWTClaimsSet getClaimsFromJwtToken(final String jwtToken) {
        try {
            String[] jwtTokenParts = jwtToken.split("\\.");
            Base64.Decoder decoder = Base64.getDecoder();
            String infoString = new String(decoder.decode(jwtTokenParts[1]));
            return JWTClaimsSet.parse(infoString);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get OID from JWT token.
     *
     * @param jwtToken the JWT token string
     * @return the OID
     */
    public String getOidFromJwtToken(final String jwtToken) {
        try {
            JWTClaimsSet claimsSet = getClaimsFromJwtToken(jwtToken);
            return claimsSet == null ? null : claimsSet.getStringClaim("oid");
        } catch (ParseException e) {
            return null;
        }
    }


    /**
     * Get client secret
     *
     * @param aadConfiguration Azure Active Directory configuration bean.
     * @param sc KeyVault client
     * @return client secrete
     */
    public static String getClientSecret(final AzureActiveDirectoryConfiguration aadConfiguration, final SecretClient sc) {
        if (aadConfiguration.getClientSecret() != null && !aadConfiguration.getClientSecret().isEmpty()) {
            return aadConfiguration.getClientSecret();
        }
        return KeyVaultFacade.getSecretWithValidation(sc, SECRET_NAME);
    }
}
