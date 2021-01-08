package org.opengroup.osdu.azure.util;

import com.nimbusds.jwt.JWTClaimsSet;

import java.text.ParseException;
import java.util.Base64;

/**
 * Utils for auth.
 */
public final class AuthUtils {
    /**
     * Private constructor.
     */
    private AuthUtils() {
    }

    /**
     * Get claims set from JWT token.
     *
     * @param jwtToken the JWT token string
     * @return the JWTClaimsSet object
     */
    public static JWTClaimsSet getClaimsFromJwtToken(final String jwtToken) {
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
    public static String getOidFromJwtToken(final String jwtToken) {
        try {
            JWTClaimsSet claimsSet = getClaimsFromJwtToken(jwtToken);
            return claimsSet == null ? null : claimsSet.getStringClaim("oid");
        } catch (ParseException e) {
            return null;
        }
    }
}
