package org.opengroup.osdu.azure;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.internal.AsyncDocumentClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *  Implementation for ICosmosClientFactory.
 */
@Component
@Lazy
public class CosmosClientFactoryImpl implements ICosmosClientFactory {

    @Lazy
    @Autowired
    private CosmosClient cosmosClient;

    @Lazy
    @Autowired
    private AsyncDocumentClient asyncDocumentClient;

    /**
     * @param dataPartitionId Data Partition Id
     * @return Cosmos Client instance
     */
    @Override
    public CosmosClient getClient(final String dataPartitionId) {
        return cosmosClient;
    }

    /**
     * @param dataPartitionId Data Partition Id
     * @return Async Document Client instance
     */
    @Override
    public AsyncDocumentClient getAsyncClient(final String dataPartitionId) {
        return asyncDocumentClient;
    }
}
