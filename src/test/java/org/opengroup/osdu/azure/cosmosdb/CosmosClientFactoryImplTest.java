package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.CosmosClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.cache.CosmosClientCache;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
public class CosmosClientFactoryImplTest {

    @Mock
    private CosmosClientCache clientCache;
    @Mock
    private PartitionServiceClient partitionService;
    @InjectMocks
    private CosmosClientFactoryImpl sut;

    private static final String PARTITION_ID = "dataPartitionId";

    @BeforeEach
    void init() {
        initMocks(this);
    }

    @Test
    public void should_throwException_given_nullDataPartitionId() {
        try {
            this.sut.getClient(null);
        } catch (NullPointerException ex) {
            assertEquals("dataPartitionId cannot be null!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_throwException_given_emptyDataPartitionId() {
        try {
            this.sut.getClient("");
        } catch (IllegalArgumentException ex) {
            assertEquals("dataPartitionId cannot be empty!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_return_cachedClient_when_cachedEarlier() {
        CosmosClient cosmosClient = mock(CosmosClient.class);
        final String cacheKey = String.format("%s-cosmosClient", PARTITION_ID);
        when(this.clientCache.containsKey(cacheKey)).thenReturn(true);
        when(this.clientCache.get(cacheKey)).thenReturn(cosmosClient);

        this.sut.getClient(PARTITION_ID);
        verify(this.partitionService, never()).getPartition(PARTITION_ID);
    }
}
