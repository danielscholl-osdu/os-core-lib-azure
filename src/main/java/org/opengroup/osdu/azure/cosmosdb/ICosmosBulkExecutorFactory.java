package org.opengroup.osdu.azure.cosmosdb;

import com.microsoft.azure.documentdb.bulkexecutor.DocumentBulkExecutor;

/**
 *  Interface for Cosmos Bulk Executor Factory to return appropriate Cosmos Bulk Executor client.
 */
public interface ICosmosBulkExecutorFactory {


    /**
     *
     * @param dataPartitionId name of the data partition.
     * @param cosmosDBName name of CosmosDB.
     * @param collectionName name of the collection in Cosmos.
     * @return the DocumentBulkExecutor.
     */
     DocumentBulkExecutor getClient(String dataPartitionId,
                                   String cosmosDBName,
                                   String collectionName);

}
