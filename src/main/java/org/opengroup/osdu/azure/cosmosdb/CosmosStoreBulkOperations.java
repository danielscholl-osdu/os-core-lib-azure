package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.azure.logging.DependencyLogger;
import org.opengroup.osdu.azure.logging.DependencyLoggingOptions;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.opengroup.osdu.azure.logging.DependencyType.COSMOS_STORE;

/**
 * Class to perform bulk Cosmos operations using CosmosClient.
 */
@Component
@Lazy
public class CosmosStoreBulkOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosStoreBulkOperations.class.getName());

    @Autowired
    private DependencyLogger dependencyLogger;

    @Autowired
    private ICosmosClientFactory cosmosClientFactory;

    /**
     * Bulk upserts item into cosmos collection using CosmosClient.
     * Partition Keys must be provided in the same order as records.
     * ith Record's partition Key will be at ith position in the List.
     *
     * @param dataPartitionId                 name of data partition.
     * @param cosmosDBName                    name of Cosmos db.
     * @param collectionName                  name of collection in Cosmos.
     * @param docs                            collection of JSON serializable documents.
     * @param partitionKeys                   List of partition keys corresponding to "docs" provided
     * @param maxConcurrencyPerPartitionRange concurrency per partition (1-5)
     * @param <T>                             Type of object being bulk inserted.
     */
    public final <T> void bulkInsertWithCosmosClient(final String dataPartitionId,
                                                     final String cosmosDBName,
                                                     final String collectionName,
                                                     final List<T> docs,
                                                     final List<String> partitionKeys,
                                                     final int maxConcurrencyPerPartitionRange) {

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            cosmosItemOperations.add(CosmosBulkOperations.getUpsertItemOperation(docs.get(i), new PartitionKey(partitionKeys.get(i))));
        }
        performBulkOperation(dataPartitionId, cosmosDBName, collectionName, cosmosItemOperations, partitionKeys, maxConcurrencyPerPartitionRange, "upsert");
    }


    /**
     * Bulk patch items into cosmos collection using CosmosClient. CosmosPatchOperations can differ for different documents, thus we are
     * using a Map<documentId, CosmosPatchOperations>. This approach is used (instead of applying the same CosmosPatchOperations on all
     * documents in the list) to avoid potential duplicates on certain document attributes (like acl/viewers, acl/owners).
     * The caller is responsible to generate this map.
     * Correct partition key for the corresponding document is stored in a map
     *
     * @param dataPartitionId                       name of data partition.
     * @param cosmosDBName                          name of Cosmos db.
     * @param collectionName                        name of collection in Cosmos.
     * @param cosmosPatchOperationsPerDoc           CosmosPatchOperations corresponding to each document
     * @param partitionKeyForDoc                    Partition keys corresponding to each document
     * @param maxConcurrencyPerPartitionRange       concurrency per partition (1-5)
     */
    public final void bulkPatchWithCosmosClient(final String dataPartitionId,
                                                     final String cosmosDBName,
                                                     final String collectionName,
                                                     final Map<String, CosmosPatchOperations> cosmosPatchOperationsPerDoc,
                                                     final Map<String, String> partitionKeyForDoc,
                                                     final int maxConcurrencyPerPartitionRange) {
        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        for (String docId : cosmosPatchOperationsPerDoc.keySet()) {
            cosmosItemOperations.add(CosmosBulkOperations.getPatchItemOperation(docId, new PartitionKey(partitionKeyForDoc.get(docId)), cosmosPatchOperationsPerDoc.get(docId)));
        }

        performBulkOperation(dataPartitionId, cosmosDBName, collectionName, cosmosItemOperations, new ArrayList(partitionKeyForDoc.values()), maxConcurrencyPerPartitionRange, "patch");
    }


    /**
     * One CosmosPatchOperations can have no more than 10 operations. To support a larger number of patch operation per doc (more than 10) we need to change
     * Map<String, CosmosPatchOperations> to Map<String, List<CosmosPatchOperations>>
     *
     * @param dataPartitionId                       name of data partition.
     * @param cosmosDBName                          name of Cosmos db.
     * @param collectionName                        name of collection in Cosmos.
     * @param cosmosPatchOperationsPerDoc           List of CosmosPatchOperations corresponding to each document
     * @param partitionKeyForDoc                    Partition keys corresponding to each document
     * @param maxConcurrencyPerPartitionRange       concurrency per partition (1-5)
     */
    public final void bulkMultiPatchWithCosmosClient(final String dataPartitionId,
                                                final String cosmosDBName,
                                                final String collectionName,
                                                final Map<String, List<CosmosPatchOperations>> cosmosPatchOperationsPerDoc,
                                                final Map<String, String> partitionKeyForDoc,
                                                final int maxConcurrencyPerPartitionRange) {

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        cosmosPatchOperationsPerDoc.forEach((docId, operations) ->
                operations.forEach(operation -> cosmosItemOperations.add(CosmosBulkOperations.getPatchItemOperation(docId, new PartitionKey(partitionKeyForDoc.get(docId)), operation))));

        performBulkOperation(dataPartitionId, cosmosDBName, collectionName, cosmosItemOperations, new ArrayList<>(partitionKeyForDoc.values()), maxConcurrencyPerPartitionRange, "patch");
    }

    /**
     * Bulk patch items into cosmos collection using CosmosClient.
     * Partition Keys must be provided in the same order as records.
     * ith Record's partition Key will be at ith position in the List.
     *
     * @param dataPartitionId                 name of data partition.
     * @param cosmosDBName                    name of Cosmos db.
     * @param collectionName                  name of collection in Cosmos.
     * @param cosmosItemOperations            List of cosmos item operations to be executed
     * @param partitionKeys                   List of partition keys corresponding to "docs" provided
     * @param maxConcurrencyPerPartitionRange concurrency per partition (1-5)
     * @param operation                       operation to be performed (i.e. upsert, patch, etc)
     */
    private void performBulkOperation(final String dataPartitionId,
                                      final String cosmosDBName,
                                      final String collectionName,
                                      final List<CosmosItemOperation> cosmosItemOperations,
                                      final List<String> partitionKeys,
                                      final int maxConcurrencyPerPartitionRange,
                                      final String operation) {
        final long start = System.currentTimeMillis();
        final int[] statusCode = {HttpStatus.SC_OK};
        final int[] clientStatusCode = {HttpStatus.SC_OK};
        final double[] requestCharge = {0.0};

        List<String> exceptions = new ArrayList<>();

        try {
            CosmosClient cosmosClient = cosmosClientFactory.getClient(dataPartitionId);
            CosmosContainer container = cosmosClient.getDatabase(cosmosDBName).getContainer(collectionName);

            CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
            cosmosBulkExecutionOptions.setMaxMicroBatchConcurrency(maxConcurrencyPerPartitionRange);

            container.executeBulkOperations(cosmosItemOperations, cosmosBulkExecutionOptions).forEach(cosmosBulkOperationResponse -> {
                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                CosmosItemOperation cosmosItemOperation = cosmosBulkOperationResponse.getOperation();
                requestCharge[0] += cosmosBulkItemResponse.getRequestCharge();

                if (cosmosBulkItemResponse != null && cosmosBulkOperationResponse.getResponse() != null && cosmosBulkOperationResponse.getResponse().isSuccessStatusCode()) {
                    LOGGER.info("Item : [{}], Status Code: {}, Request Charge: {}", cosmosItemOperation.getItem().toString(), cosmosBulkItemResponse.getStatusCode(), cosmosBulkItemResponse.getRequestCharge());
                } else {
                    LOGGER.error(
                            "The operation for Item : [{}] Failed. Response code : {}. , Request Charge: {}, Exception : {}",
                            cosmosItemOperation.getId(),
                            cosmosBulkItemResponse.getStatusCode(),
                            cosmosBulkItemResponse.getRequestCharge(),
                            cosmosBulkOperationResponse.getException());
                    if (cosmosBulkOperationResponse.getException() != null) {
                        exceptions.add(cosmosItemOperation.getId() + "| Error occurred for " + operation + " operation with status code " + cosmosBulkItemResponse.getStatusCode() + "| Exception is: " + cosmosBulkOperationResponse.getException().toString());
                    } else {
                        exceptions.add(cosmosItemOperation.getId() + "| Internal error occurred for " + operation + " operation with status code " + cosmosBulkItemResponse.getStatusCode());
                    }
                    if (cosmosBulkItemResponse.getStatusCode() >= 500) {
                        statusCode[0] = cosmosBulkItemResponse.getStatusCode();
                    } else if (cosmosBulkItemResponse.getStatusCode() >= 400) {
                        clientStatusCode[0] = cosmosBulkItemResponse.getStatusCode();
                    }
                }
            });

            if (!exceptions.isEmpty()) {
                int status = statusCode[0] != HttpStatus.SC_OK ? statusCode[0] : clientStatusCode[0];
                LOGGER.error("Failed to " + operation + " documents in CosmosDB: {}", String.join(",", exceptions));
                throw new AppException(status, "Record " + operation + " has failed!", "Failed to " + operation + " documents in CosmosDB", exceptions.toArray(new String[exceptions.size()]));
            }
        } catch (Exception e) {
            int status = statusCode[0] != HttpStatus.SC_OK ? statusCode[0] : clientStatusCode[0];
            String errorMessage = "Unexpectedly failed to bulk " + operation + " documents";
            LOGGER.error(errorMessage, e);
            throw new AppException(status, errorMessage, e.getMessage(), e);
        } finally {
            int status = statusCode[0] != HttpStatus.SC_OK ? statusCode[0] : clientStatusCode[0];
            final long timeTaken = System.currentTimeMillis() - start;
            final String dependencyTarget = DependencyLogger.getCosmosDependencyTarget(cosmosDBName, collectionName);
            final String dependencyData = String.format("partition_key=%s", new HashSet<>(partitionKeys));
            final String operationItems = operation + "_items";
            final DependencyLoggingOptions loggingOptions = DependencyLoggingOptions.builder()
                    .type(COSMOS_STORE)
                    .name(operationItems.toUpperCase())
                    .data(dependencyData)
                    .target(dependencyTarget)
                    .timeTakenInMs(timeTaken)
                    .requestCharge(requestCharge[0])
                    .resultCode(statusCode[0])
                    .success(status == HttpStatus.SC_OK)
                    .build();
            dependencyLogger.logDependency(loggingOptions);
        }
    }
}
