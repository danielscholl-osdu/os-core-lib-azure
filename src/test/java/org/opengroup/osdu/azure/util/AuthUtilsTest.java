package org.opengroup.osdu.azure.util;

import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AuthUtilsTest {

    // Mock JWT Token: {"aud": "audience","appid": "application-id","oid": "my-oid","sub": "my-sub","tid": "my-tenant"}
    private static final String jwtToken = "part1.eyJhdWQiOiJhdWRpZW5jZSIsImFwcGlkIjoiYXBwbGljYXRpb24taWQiLCJvaWQiOiJteS1vaWQiLCJzdWIiOiJteS1zdWIiLCJ0aWQiOiJteS10ZW5hbnQifQ.part3";
    private static final String invalidJwtToken = "invalidjwttoken";
    private static final String invalidJwtTokenClaims = "part1.invalidclaims.part2";
    private static final String emptyJwtTokenClaims = "part1.e30.part3";

    @InjectMocks
    private AuthUtils authUtils;

    @Test
    public void ShouldSuccessfullyGetClaims() throws Exception {
        JWTClaimsSet claims = authUtils.getClaimsFromJwtToken(jwtToken);

        assertEquals(5, claims.getClaims().size());
        assertEquals(1, claims.getAudience().size());
        assertEquals("audience", claims.getAudience().get(0));
        assertEquals("my-sub", claims.getSubject());
        assertEquals("application-id", claims.getStringClaim("appid"));
        assertEquals("my-oid", claims.getStringClaim("oid"));
        assertEquals("my-tenant", claims.getStringClaim("tid"));
    }

    @Test
    public void ShouldReturnNullForInvalidToken() throws Exception {
        JWTClaimsSet claims = authUtils.getClaimsFromJwtToken(invalidJwtToken);
        assertEquals(null, claims);
    }

    @Test
    public void ShouldReturnNullForInvalidTokenClaims() throws Exception {
        JWTClaimsSet claims = authUtils.getClaimsFromJwtToken(invalidJwtTokenClaims);
        assertEquals(null, claims);
    }

    @Test
    public void ShouldReturnEmptySetForEmptyTokenClaims() throws Exception {
        JWTClaimsSet claims = authUtils.getClaimsFromJwtToken(emptyJwtTokenClaims);
        assertEquals(0, claims.getClaims().size());
    }

    @Test
    public void ShouldReturnOidForValidTokenWithOid() throws Exception {
        String oid = authUtils.getOidFromJwtToken(jwtToken);
        assertEquals("my-oid", oid);
    }

    @Test
    public void ShouldReturnNullOidForInvalidToken() throws Exception {
        String oid = authUtils.getOidFromJwtToken(invalidJwtToken);
        assertEquals(null, oid);
    }

    @Test
    public void ShouldReturnNullOidForEmptyToken() throws Exception {
        String oid = authUtils.getOidFromJwtToken(emptyJwtTokenClaims);
        assertEquals(null, oid);
    }
}
