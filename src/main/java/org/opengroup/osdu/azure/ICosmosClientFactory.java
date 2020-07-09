package org.opengroup.osdu.azure;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.internal.AsyncDocumentClient;

/**
 *  Interface for Cosmos Client Factory to return appropriate cosmos client.
 *  instances for each tenant based on data partition id
 */
public interface ICosmosClientFactory {

    /**
     * @param dataPartitionId Data Partition Id
     * @return Cosmos client instance
     */
    CosmosClient getClient(String dataPartitionId);

    /**
     * @param dataPartitionId Data Partition Id
     * @return Async Document Client instance
     */
    AsyncDocumentClient getAsyncClient(String dataPartitionId);
}
