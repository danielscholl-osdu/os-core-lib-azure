package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.google.gson.Gson;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.bulkexecutor.BulkImportResponse;
import com.microsoft.azure.documentdb.bulkexecutor.DocumentBulkExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.logging.DependencyLogger;
import org.opengroup.osdu.azure.logging.DependencyLoggingOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.opengroup.osdu.azure.logging.DependencyType.COSMOS_STORE;

@ExtendWith(MockitoExtension.class)
public class CosmosBulkExecutorImplTest {

    private static final String COSMOS_DB = "cosmosdb";
    private static final String COLLECTION = "collection";
    private static final String DATA_PARTITION_ID = "data-partition-id";
    private static final String ITEM = "ITEM";

    private Gson gson = new Gson();

    @Mock
    private DependencyLogger dependencyLogger;
    @Mock
    private ICosmosClientFactory cosmosClientFactory;
    @Mock
    private ICosmosBulkExecutorFactory bulkExecutorFactory;
    @InjectMocks
    private CosmosStoreBulkOperations sut;

    @BeforeEach
    void init() {
        openMocks(this);
    }

    @Test
    public void bulkUpsert_Success() throws DocumentClientException {
        DocumentBulkExecutor documentBulkExecutor = mock(DocumentBulkExecutor.class);
        BulkImportResponse bulkImportResponse = mock(BulkImportResponse.class);
        List<String> documents = singletonList(ITEM);
        Collection<String> serializedDocuments = new ArrayList<>();
        for (String item : documents) {
            String serializedDocument = gson.toJson(item);
            serializedDocuments.add(serializedDocument);
        }
        lenient().doReturn(1.0).when(bulkImportResponse).getTotalRequestUnitsConsumed();
        lenient().doReturn(bulkImportResponse).when(documentBulkExecutor).importAll(serializedDocuments, false, false, 1);
        when(this.bulkExecutorFactory.getClient(DATA_PARTITION_ID, COSMOS_DB, COLLECTION)).thenReturn(documentBulkExecutor);
        ArgumentCaptor<DependencyLoggingOptions> loggingOptionsArgumentCaptor = ArgumentCaptor.forClass(DependencyLoggingOptions.class);

        this.sut.bulkInsert(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, documents, false, false, 1);

        verify(this.bulkExecutorFactory, times(1)).getClient(DATA_PARTITION_ID, COSMOS_DB, COLLECTION);
        verify(dependencyLogger, times(1)).logDependency(loggingOptionsArgumentCaptor.capture());
        DependencyLoggingOptions actualLoggingOptions = loggingOptionsArgumentCaptor.getValue();
        verifyDependencyLogging(actualLoggingOptions, "UPSERT_ITEMS", "collectionName=collection", "cosmosdb/collection", 1.0, 200, true);
    }

    @Test
    public void bulkPatch_Success() throws DocumentClientException {
        CosmosClient cosmosClient = mock(CosmosClient.class);
        CosmosContainer cosmosContainer = mock(CosmosContainer.class);
        CosmosPatchOperations cosmosPatchOperations = mock(CosmosPatchOperations.class);
        CosmosItemOperation cosmosItemOperation = mock(CosmosItemOperation.class);
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = mock(CosmosBulkExecutionOptions.class);
        cosmosBulkExecutionOptions.setMaxMicroBatchConcurrency(1);
        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        cosmosItemOperations.add(cosmosItemOperation);
        CosmosBulkOperationResponse cosmosBulkOperationResponse = mock(CosmosBulkOperationResponse.class);
        List<CosmosBulkOperationResponse> bulkPatchResponse = new ArrayList<>();
        bulkPatchResponse.add(cosmosBulkOperationResponse);
        Map<String, CosmosPatchOperations> cosmosPatchOperationsPerDoc = new HashMap<>();
        cosmosPatchOperationsPerDoc.put("id1", cosmosPatchOperations);
        Map<String, String> partitionKeyForDoc = new HashMap<>();
        partitionKeyForDoc.put("id1", "id1");

        lenient().doReturn(cosmosClient).when(cosmosClientFactory).getClient(DATA_PARTITION_ID);
        CosmosBulkItemResponse cosmosBulkItemResponse = mock(CosmosBulkItemResponse.class);
        lenient().doReturn(cosmosBulkItemResponse).when(cosmosBulkOperationResponse).getResponse();
        lenient().doReturn(bulkPatchResponse).when(cosmosContainer).executeBulkOperations(cosmosItemOperations, cosmosBulkExecutionOptions);
        CosmosDatabase cosmosDatabase = mock(CosmosDatabase.class);
        when(cosmosClient.getDatabase(COSMOS_DB)).thenReturn(cosmosDatabase);
        when(cosmosDatabase.getContainer(COLLECTION)).thenReturn(cosmosContainer);
        ArgumentCaptor<DependencyLoggingOptions> loggingOptionsArgumentCaptor = ArgumentCaptor.forClass(DependencyLoggingOptions.class);

        this.sut.bulkPatchWithCosmosClient(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, cosmosPatchOperationsPerDoc, partitionKeyForDoc, 1);

        verify(this.cosmosClientFactory, times(1)).getClient(DATA_PARTITION_ID);
        verify(dependencyLogger, times(1)).logDependency(loggingOptionsArgumentCaptor.capture());
        DependencyLoggingOptions actualLoggingOptions = loggingOptionsArgumentCaptor.getValue();
        verifyDependencyLogging(actualLoggingOptions, "PATCH_ITEMS", "partition_key=[id1]", "cosmosdb/collection", 0.0, 200, true);
    }

    private void verifyDependencyLogging(DependencyLoggingOptions capturedLoggingOptions, String name, String data, String target, double requestCharge, int resultCode, boolean success) {
        assertEquals(COSMOS_STORE, capturedLoggingOptions.getType());
        assertEquals(name, capturedLoggingOptions.getName());
        assertEquals(data, capturedLoggingOptions.getData());
        assertEquals(target, capturedLoggingOptions.getTarget());
        assertEquals(requestCharge, capturedLoggingOptions.getRequestCharge());
        assertEquals(resultCode, capturedLoggingOptions.getResultCode());
        assertEquals(success, capturedLoggingOptions.isSuccess());
    }
}
