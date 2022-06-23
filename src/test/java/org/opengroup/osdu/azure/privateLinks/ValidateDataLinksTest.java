package org.opengroup.osdu.azure.privateLinks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidateDataLinksTest {



    /*
    Tests to cover

    1. ipv6 with 10th bit as 0
    2. ipv6 with 10th bit as 1 not present in cache, should be present in DB
    3. ipv6 with 10th bit as 1 not present in DB

     */


    @InjectMocks
    ValidateDataLinks validateDataLinks;

    @Mock
    CosmosStore cosmosStore;

    @Test
    public void testWithIpv6HavingBit0() {

        String ipv6 = "FE80:CD00:A:CDE:1257:0:211E:729C";
        boolean response = validateDataLinks.validateRequest(ipv6);
        assertFalse(response);

    }

    @Test
    public void testWithIpv6HavingBit1PresentInDB(){
        String ipv6 = "FEC0:CD00:A:CDE:1257:0:211E:729C";

        when(cosmosStore.findItem(any(),any(),any(),any(),any())).thenReturn(Optional.of(345666));
        boolean response =  validateDataLinks.validateRequest(ipv6);

        assertTrue(response);
    }

    @Test
    public void testWithIpv6HavingBit1NotPresentInDB(){
        String ipv6 = "FEC0:CD00:A:CDE:1257:0:211E:729C";

        when(cosmosStore.findItem(any(),any(),any(),any(),any())).thenReturn(Optional.empty());
        boolean response =  validateDataLinks.validateRequest(ipv6);

        assertFalse(response);
    }
}
