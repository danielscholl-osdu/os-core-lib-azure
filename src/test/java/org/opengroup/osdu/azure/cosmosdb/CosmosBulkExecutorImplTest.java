package org.opengroup.osdu.azure.cosmosdb;

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
import java.util.List;

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
        assertEquals(COSMOS_STORE, actualLoggingOptions.getType());
        assertEquals("UPSERT_ITEMS", actualLoggingOptions.getName());
        assertEquals("collectionName=collection", actualLoggingOptions.getData());
        assertEquals("cosmosdb/collection", actualLoggingOptions.getTarget());
        assertEquals(1.0, actualLoggingOptions.getRequestCharge());
        assertEquals(200, actualLoggingOptions.getResultCode());
        assertEquals(true, actualLoggingOptions.isSuccess());
    }
}
