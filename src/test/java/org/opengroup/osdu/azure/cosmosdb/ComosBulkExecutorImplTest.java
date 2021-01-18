package org.opengroup.osdu.azure.cosmosdb;

import com.microsoft.azure.documentdb.bulkexecutor.DocumentBulkExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.cache.CosmosBulkExecutorCache;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
public class ComosBulkExecutorImplTest {

    @Mock
    private CosmosBulkExecutorCache clientCache;
    @Mock
    private PartitionServiceClient partitionService;
    @InjectMocks
    private CosmosBulkExecutorFactoryImpl sut;

    private static final String PARTITION_ID = "dataPartitionId";
    private static final String COSMOS_DB_NAME = "cosmosDBName";
    private static final String COSMOS_COLLECTION_NAME = "cosmosCollectionName";

    @BeforeEach
    void init() {
        initMocks(this);
    }

    @Test
    public void should_throwException_given_nullDataPartitionId() {
        try {
            this.sut.getClient(null, COSMOS_DB_NAME, COSMOS_COLLECTION_NAME);
        } catch (NullPointerException ex) {
            assertEquals("dataPartitionId cannot be null!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_throwException_given_emptyDataPartitionId() {
        try {
            this.sut.getClient("", COSMOS_DB_NAME, COSMOS_COLLECTION_NAME);
        } catch (IllegalArgumentException ex) {
            assertEquals("dataPartitionId cannot be empty!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_return_cachedClient_when_cachedEarlier() {
        DocumentBulkExecutor cosmosClient = mock(DocumentBulkExecutor.class);
        final String cacheKey = String.format("%s-%s-%s-cosmosBulkExecutor", PARTITION_ID, COSMOS_DB_NAME, COSMOS_COLLECTION_NAME);
        when(this.clientCache.containsKey(cacheKey)).thenReturn(true);
        when(this.clientCache.get(cacheKey)).thenReturn(cosmosClient);

        this.sut.getClient(PARTITION_ID, COSMOS_DB_NAME, COSMOS_COLLECTION_NAME);
        verify(this.partitionService, never()).getPartition(PARTITION_ID);
    }

}
