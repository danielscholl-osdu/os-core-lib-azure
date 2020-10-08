package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.opengroup.osdu.azure.cache.CosmosClientCache;
import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.common.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation for ICosmosClientFactory.
 */
@Component
@Lazy
public class CosmosClientFactoryImpl implements ICosmosClientFactory {

    @Lazy
    @Autowired
    private PartitionServiceClient partitionService;

    @Lazy
    @Autowired
    private CosmosClientCache syncClientCache;

    /**
     * @param dataPartitionId Data Partition Id
     * @return Cosmos Client instance
     */
    @Override
    public CosmosClient getClient(final String dataPartitionId) {
        Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");

        String cacheKey = String.format("%s-cosmosClient", dataPartitionId);
        if (this.syncClientCache.containsKey(cacheKey)) {
            return this.syncClientCache.get(cacheKey);
        }

        PartitionInfoAzure pi = this.partitionService.getPartition(dataPartitionId);
        CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(pi.getCosmosEndpoint())
                .key(pi.getCosmosPrimaryKey())
                .buildClient();

        this.syncClientCache.put(cacheKey, cosmosClient);

        return cosmosClient;
    }

}
