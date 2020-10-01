package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.internal.AsyncDocumentClient;
import org.opengroup.osdu.azure.cache.AsyncCosmosClientCache;
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

    @Lazy
    @Autowired
    private AsyncCosmosClientCache asyncCosmosClientCache;

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
                .setEndpoint(pi.getCosmosEndpoint())
                .setKey(pi.getCosmosPrimaryKey())
                .buildClient();

        this.syncClientCache.put(cacheKey, cosmosClient);

        return cosmosClient;
    }

    /**
     * @param dataPartitionId Data Partition Id
     * @return Async Document Client instance
     */
    @Override
    public AsyncDocumentClient getAsyncClient(final String dataPartitionId) {
        Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");

        String cacheKey = String.format("%s-asyncCosmosClient", dataPartitionId);
        if (this.syncClientCache.containsKey(cacheKey)) {
            return this.asyncCosmosClientCache.get(cacheKey);
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DIRECT);

        PartitionInfoAzure pi = this.partitionService.getPartition(dataPartitionId);
        AsyncDocumentClient client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(pi.getCosmosEndpoint())
                .withMasterKeyOrResourceToken(pi.getCosmosPrimaryKey())
                .withConnectionPolicy(connectionPolicy)
                .build();

        this.asyncCosmosClientCache.put(cacheKey, client);

        return client;
    }
}
