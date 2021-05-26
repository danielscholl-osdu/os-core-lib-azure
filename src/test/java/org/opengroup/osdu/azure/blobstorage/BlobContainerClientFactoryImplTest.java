package org.opengroup.osdu.azure.blobstorage;

import com.azure.identity.DefaultAzureCredential;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.cache.BlobContainerClientCache;
import org.opengroup.osdu.azure.di.BlobStoreConfiguration;
import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.core.common.partition.Property;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
public class BlobContainerClientFactoryImplTest {

    @Mock
    private DefaultAzureCredential credential;
    @Mock
    private PartitionServiceClient partitionService;
    @Mock
    private BlobContainerClientCache clientCache;
    @Mock
    private BlobStoreConfiguration configuration;
    @InjectMocks
    private BlobContainerClientFactoryImpl sut;

    private static final String ACCOUNT_NAME = "testAccount";
    private static final String PARTITION_ID = "dataPartitionId";
    private static final String STORAGE_CONTAINER_NAME = "containerName";

    @BeforeEach
    void init() {
        initMocks(this);
        lenient().doReturn(ACCOUNT_NAME).when(configuration).getStorageAccountName();
    }

    @Test
    public void should_throwException_given_nullDataPartitionId() {
        try {
            this.sut.getClient(null, "testContainer");
        } catch (NullPointerException ex) {
            assertEquals("dataPartitionId cannot be null!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_throwException_given_emptyDataPartitionId() {
        try {
            this.sut.getClient("", "");
        } catch (IllegalArgumentException ex) {
            assertEquals("dataPartitionId cannot be empty!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_return_validContainer_given_validPartitionId() {
        when(this.partitionService.getPartition(PARTITION_ID)).thenReturn(
                PartitionInfoAzure.builder()
                        .idConfig(Property.builder().value(PARTITION_ID).build())
                        .storageAccountNameConfig(Property.builder().value(ACCOUNT_NAME).build()).build());

        BlobContainerClient containerClient = this.sut.getClient(PARTITION_ID, STORAGE_CONTAINER_NAME);
        assertNotNull(containerClient);
    }

    @Test
    public void should_return_cachedContainer_when_cachedEarlier() {
        when(this.partitionService.getPartition(PARTITION_ID)).thenReturn(
                PartitionInfoAzure.builder()
                        .idConfig(Property.builder().value(PARTITION_ID).build())
                        .storageAccountNameConfig(Property.builder().value(ACCOUNT_NAME).build()).build());
        BlobContainerClient containerClient = this.sut.getClient(PARTITION_ID, STORAGE_CONTAINER_NAME);
        assertNotNull(containerClient);

        final String cacheKey = String.format("%s-%s", PARTITION_ID, STORAGE_CONTAINER_NAME);
        when(this.clientCache.containsKey(cacheKey)).thenReturn(true);
        when(this.clientCache.get(cacheKey)).thenReturn(containerClient);

        this.sut.getClient(PARTITION_ID, STORAGE_CONTAINER_NAME);
        verify(this.partitionService, times(1)).getPartition(PARTITION_ID);
    }
}
