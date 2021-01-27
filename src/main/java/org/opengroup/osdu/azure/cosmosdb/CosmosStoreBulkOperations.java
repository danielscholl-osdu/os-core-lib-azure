package org.opengroup.osdu.azure.cosmosdb;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.bulkexecutor.BulkImportResponse;
import com.microsoft.azure.documentdb.bulkexecutor.DocumentBulkExecutor;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class to perform bulk Cosmos operations using DocumentBulkExecutor.
 */
@Component
@Lazy
public class CosmosStoreBulkOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosStoreBulkOperations.class.getName());

    @Autowired
    private ICosmosBulkExecutorFactory bulkExecutorFactory;

    /**
     *
     * Bulk upserts item into cosmos collection.
     * @param dataPartitionId name of data partition.
     * @param cosmosDBName name of Comsos db.
     * @param collectionName name of collection in Cosmos.
     * @param documents collection of JSON serializable documents.
     * @param isUpsert flag denoting if the isUpsert flag should be set to true.
     * @param disableAutomaticIdGeneration flag denoting if automatic id generation should be disabled in Cosmos.
     * @param maxConcurrencyPerPartitionRange The maximum degree of concurrency per partition key range. The default value is 20.
     * @param <T> Type of object being bulk inserted.
     * @return BulkImportResponse object with the results of the operation.
     */
    public final <T> BulkImportResponse bulkInsert(final String dataPartitionId,
                                                   final String cosmosDBName,
                                                   final String collectionName,
                                                   final Collection<T> documents,
                                                   final boolean isUpsert,
                                                   final boolean disableAutomaticIdGeneration,
                                                   final int maxConcurrencyPerPartitionRange) {
        Collection<String> serializedDocuments = new ArrayList<>();
        Gson gson = new Gson();

        // Serialize documents to json strings
        for (T item : documents) {
            String serializedDocument = gson.toJson(item);
            serializedDocuments.add(serializedDocument);
        }

        try {
            DocumentBulkExecutor executor = bulkExecutorFactory.getClient(dataPartitionId, cosmosDBName, collectionName);
            BulkImportResponse response = executor.importAll(serializedDocuments, isUpsert, disableAutomaticIdGeneration, maxConcurrencyPerPartitionRange);

            if (response.getNumberOfDocumentsImported() != documents.size()) {
                LOGGER.warn("Failed to import all documents using DocumentBulkExecutor! Attempted to import " + documents.size() + " documents but only imported " + response.getNumberOfDocumentsImported());
            }
            return response;
        } catch (DocumentClientException e) {
            String errorMessage = "Unexpectedly failed to bulk insert documents";
            LOGGER.warn(errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }
}
