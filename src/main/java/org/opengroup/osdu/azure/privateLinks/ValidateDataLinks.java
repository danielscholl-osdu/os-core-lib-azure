package org.opengroup.osdu.azure.privateLinks;

import com.azure.cosmos.models.SqlQuerySpec;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
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
public class ValidateDataLinks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateDataLinks.class);
    private static final String COSMOS_DB = "PrivateLinkDB";
    private static final String DATA_PARTITION_ID = "PrivateLinkID";
    private static final String COLLECTION = "PrivateLinkCollection";


    @Autowired
    private CosmosStore cosmosStore;

    private List<Long> cache = new ArrayList<>();

    /**
     *
     * @param ipv6 String
     * @return boolean
     * @throws UnknownHostException
     */
    public boolean validateRequest(final String ipv6)  {
        boolean result = true;
        boolean finished = false;
        byte[] bytes = new byte[0];
        try {
            bytes = InetAddress.getByName(ipv6).getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String ipAddressInBits = new BigInteger(1, bytes).toString(2);

        if (ipAddressInBits.charAt(9) == '1') {
            LOGGER.info("Ipaddress 10th bit is 1");
            // fetch private link from ipv6 address. It starts from 17th bit and is 32 bit length

            String privateLinkString = ipAddressInBits.substring(16, 48);
            Long privateLinkID = Long.parseLong(privateLinkString, 2);

            //check if present in cache?

            if (isPresentInCache(privateLinkID)) {
                LOGGER.info("Present in cache");
                finished = true;
            } else {

                /* call to db */

                LOGGER.info("Calling to db");
                Optional<Long> optionalPrivateLink = cosmosStore.findItem(COSMOS_DB, COLLECTION, String.valueOf(privateLinkID), String.valueOf(privateLinkID), Long.class);
                if (optionalPrivateLink.isPresent()) {
                    LOGGER.info("Found in DB");
                    cache.add(optionalPrivateLink.get());
                    finished = true;
                } else {
                    LOGGER.error("Private link Id not found in DB");
                    result = false;
                    finished = true;
                }
            }
        }
        if (!finished) {
            result = false;

        }
        return result;
    }

    /**
     *
     * @param privateLinkId long
     * @return boolean
     */
    private boolean isPresentInCache(final Long privateLinkId) {
        return cache.contains(privateLinkId);
    }


    /**
     * This function is for refreshing the cache after every 60 min.
     */
    @Scheduled(fixedRate = 6000)
    void cacheSyncUp() {
        String queryText = "SELECT * FROM c WHERE 1=1 ";
        SqlQuerySpec query = new SqlQuerySpec(queryText);

        cache = cosmosStore.queryItems(COSMOS_DB, COLLECTION, query, null, Long.class);
        LOGGER.info("Syncing up cache with DB");

    }
}
