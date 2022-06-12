package org.opengroup.osdu.azure.privateLinks;

import com.azure.cosmos.models.SqlQuerySpec;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.opengroup.osdu.azure.cosmosdb.system.config.SystemCosmosConfig;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.SchedulingConfiguration;

import javax.xml.ws.Response;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(value = "privateLinks.validateLinks.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class ValidateDataLinks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateDataLinks.class);
    private final String COSMOS_DB="";
    private final String DATA_PARTITION_ID="";
    private final String COLLECTION="PrivateLinkCollection";

    @Autowired
    SystemCosmosConfig systemCosmosConfig;

    @Autowired
    CosmosStore cosmosStore;

    List<Long> cache = new ArrayList<>();

   public  boolean validateRequest(String ipv6) throws UnknownHostException {
        byte[] bytes = InetAddress.getByName(ipv6).getAddress();
        String bits = new BigInteger(1,bytes).toString(2);

        if(bits.length() != 128){
            LOGGER.warn("Ipv6 address is less than 128 bit");
            return false;
        }
         if(bits.charAt(9) == '1'){
             // fetch private link from ipv6 address. It starts from 17th bit and is 32 bit length
                String privateLinkString  = ipv6.substring(16, 48);
                Long privateLinkDecimal = Long.parseLong(privateLinkString,2);

                //check if present in cosmosDB?

             /*
                we don't need to validate the storage pvt link

                which CosmosDB?  SystemCosmosDB -> ComputeRG -> storing pvt link identifier
                CosmosDB Schema? pvt link identifier
                cache -> List
                Approach? ->
                Accessing CosmosDB?
                if (cacheCheck()){
                }
                else{
                }
              */

             if( isPresentInCache(privateLinkDecimal))
                 return true;
             else{
                Long privateLink = null;
                /* call to db */

                if(privateLink !=null)
                    cache.add(privateLink);
                else {
                    LOGGER.error("Private link not found in DB");
                    return false;
                }
             }
            }
         return false;

        }

    private boolean isPresentInCache(Long privateLinkDecimal) {
        return cache.contains(privateLinkDecimal);
    }


    @Scheduled( fixedRate = 6000)
    void cacheSyncUp(){
        String queryText = "SELECT * FROM c WHERE 1=1 ";
        SqlQuerySpec query = new SqlQuerySpec(queryText);

        cache = cosmosStore.queryItems(COSMOS_DB,COLLECTION,query,null, Long.class);
        LOGGER.info("Syncing up cache with DB");

    }
}
