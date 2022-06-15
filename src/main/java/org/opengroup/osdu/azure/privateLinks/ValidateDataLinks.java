package org.opengroup.osdu.azure.privateLinks;

import com.azure.cosmos.models.SqlQuerySpec;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.azure.cosmosdb.system.config.SystemCosmosConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@EnableScheduling
public class ValidateDataLinks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateDataLinks.class);
    private final String COSMOS_DB = "PrivateLinkDB";
    private final String DATA_PARTITION_ID = "PrivateLinkID";
    private final String COLLECTION = "PrivateLinkCollection";

    @Autowired
    SystemCosmosConfig systemCosmosConfig;

    @Autowired
    CosmosStore cosmosStore;

    List<Long> cache = new ArrayList<>();

    public boolean validateRequest(String ipv6) throws UnknownHostException {
        byte[] bytes = InetAddress.getByName(ipv6).getAddress();
        String ipAddressInBits = new BigInteger(1, bytes).toString(2);

        if (ipAddressInBits.charAt(9) == '1') {
            // fetch private link from ipv6 address. It starts from 17th bit and is 32 bit length

            String privateLinkString = ipAddressInBits.substring(16, 48);
            Long privateLinkID = Long.parseLong(privateLinkString, 2);

            //check if present in cache?

            if (isPresentInCache(privateLinkID))
                return true;
            else {

                /* call to db */

                Optional<Long> optionalPrivateLink = cosmosStore.findItem(COSMOS_DB, COLLECTION, String.valueOf(privateLinkID),String.valueOf(privateLinkID),Long.class);

                if ( optionalPrivateLink.isPresent()) {
                    cache.add(optionalPrivateLink.get());
                    return true;
                }
                else {
                    LOGGER.error("Private link Id not found in DB");
                    return false;
                }
            }
        }
        return false;

    }

    private boolean isPresentInCache(Long privateLinkId) {
        return cache.contains(privateLinkId);
    }


    @Scheduled(fixedRate = 6000)
    void cacheSyncUp() {
        String queryText = "SELECT * FROM c WHERE 1=1 ";
        SqlQuerySpec query = new SqlQuerySpec(queryText);

        cache = cosmosStore.queryItems(COSMOS_DB, COLLECTION, query, null, Long.class);
        LOGGER.info("Syncing up cache with DB");

    }
}
