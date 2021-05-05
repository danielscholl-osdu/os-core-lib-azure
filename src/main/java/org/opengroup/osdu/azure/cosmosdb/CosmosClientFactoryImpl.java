package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.common.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for ICosmosClientFactory.
 */
@Component
@Lazy
public class CosmosClientFactoryImpl implements ICosmosClientFactory {

    @Lazy
    @Autowired
    private PartitionServiceClient partitionService;

    private Map<String, CosmosClient> cosmosClientMap;

    /**
     * Initializes the private variables as required.
     */
    @PostConstruct
    public void initialize() {
        cosmosClientMap = new HashMap<>();
    }

    /**
     * @param dataPartitionId Data Partition Id
     * @return Cosmos Client instance
     */
    @Override
    public CosmosClient getClient(final String dataPartitionId) {
        Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");

        String cacheKey = String.format("%s-cosmosClient", dataPartitionId);
        if (this.cosmosClientMap.containsKey(cacheKey)) {
            return this.cosmosClientMap.get(cacheKey);
        }

        PartitionInfoAzure pi = this.partitionService.getPartition(dataPartitionId);
        CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(pi.getCosmosEndpoint())
                .key(pi.getCosmosPrimaryKey())
                .buildClient();

        this.cosmosClientMap.put(cacheKey, cosmosClient);

        return cosmosClient;
    }
}
