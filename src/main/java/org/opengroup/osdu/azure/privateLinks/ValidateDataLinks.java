package org.opengroup.osdu.azure.privateLinks;

//import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.SqlQuerySpec;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is for validating private link id coming from client ipaddress.
 */
@Component
@EnableScheduling
@ConditionalOnProperty(value = "validate.privateLink.enabled", havingValue = "true", matchIfMissing = false)
public class ValidateDataLinks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateDataLinks.class);
    private static final String COSMOS_DB = "private-link-db";
    private static final String COLLECTION = "PrivateLinkCollection";


    @Autowired
    private CosmosStore cosmosStore;

    private List<PrivateLinkResponse> cache = new ArrayList<>();

    /**
     * @param ipv6 String
     * @return boolean
     */
    public boolean validateRequest(final String ipv6) {

        LOGGER.info("Validating the request");
        byte[] bytes = new byte[0];

        try {
            bytes = InetAddress.getByName(ipv6).getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        String ipAddressInBits = new BigInteger(1, bytes).toString(2);

        /*
        The 10th bit of the IPv6 address contains a flag which denotes the traffic is from private link. A 32-bit link identifier is encoded starting from 17th bit.
         */
        if (ipAddressInBits.charAt(9) == '1') {
            LOGGER.debug("Traffic is from private link");

            // fetch private link from ipv6 address. It starts from 17th bit and is 32 bit length

            String privateLinkStringInBits = ipAddressInBits.substring(16, 48);
            Long privateLinkID = Long.parseLong(privateLinkStringInBits, 2);

            //check if present in cache?

            if (isPresentInCache(privateLinkID)) {
                LOGGER.debug("PrivateLinkID Present in cache");
                return true;
            } else {

                /* call to db */

                LOGGER.debug("Searching for private link in DB ");

                Optional<PrivateLinkResponse> optionalPrivateLink = cosmosStore.findItem(COSMOS_DB, COLLECTION, String.valueOf(privateLinkID), String.valueOf(privateLinkID), PrivateLinkResponse.class);

                if (optionalPrivateLink.isPresent()) {
                    LOGGER.debug("Found private link id in DB");
                    cache.add(optionalPrivateLink.get());
                    return true;
                } else {
                    LOGGER.error("Private link id not found in DB");
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * @param privateLinkId long
     * @return boolean
     */
    public boolean isPresentInCache(final Long privateLinkId) {
        if (!cache.isEmpty()) {
            for (PrivateLinkResponse privateLinkResponse: cache) {
                if (privateLinkResponse.getId().equals(privateLinkId.toString())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * This function is for refreshing the cache after every 60 min. 1hr -> 3600 sec -> 3600 * 1000 ms
     */
    @Scheduled(fixedRate = 3600000)
    void cacheSyncUp() {
        String queryText = "SELECT * FROM c WHERE 1=1 ";
        SqlQuerySpec query = new SqlQuerySpec(queryText);

        cache = cosmosStore.queryItems(COSMOS_DB, COLLECTION, query, null, PrivateLinkResponse.class);
        LOGGER.info("Syncing up cache with DB ");
    }
}

