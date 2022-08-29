package org.opengroup.osdu.azure.cosmosdb;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.bulkexecutor.BulkImportResponse;
import com.microsoft.azure.documentdb.bulkexecutor.DocumentBulkExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.logging.DependencyLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
        lenient().doReturn(bulkImportResponse).when(documentBulkExecutor).importAll(serializedDocuments, false, false, 1);
        when(this.bulkExecutorFactory.getClient(DATA_PARTITION_ID, COSMOS_DB, COLLECTION)).thenReturn(documentBulkExecutor);

        this.sut.bulkInsert(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, documents, false, false, 1);

        verify(this.bulkExecutorFactory, times(1)).getClient(DATA_PARTITION_ID, COSMOS_DB, COLLECTION);
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("UPSERT_ITEMS"), eq("collectionName=collection"), eq(null), anyLong(), eq(200), eq(true));
    }
}
