// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.azure.blobstorage;

import com.azure.identity.DefaultAzureCredential;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.common.policy.RequestRetryOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.cache.BlobServiceClientCache;
import org.opengroup.osdu.azure.di.BlobStoreConfiguration;
import org.opengroup.osdu.azure.di.BlobStoreRetryConfiguration;
import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.common.partition.Property;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
public class BlobServiceClientFactoryImplTest {

    @Mock
    private DefaultAzureCredential credential;
    @Mock
    private PartitionServiceClient partitionService;
    @Mock
    private BlobServiceClientCache clientCache;
    @Mock
    private BlobStoreConfiguration configuration;
    @Mock
    private BlobStoreRetryConfiguration blobStoreRetryConfiguration;
    @Mock
    private ILogger logger;
    @InjectMocks
    private BlobServiceClientFactoryImpl sut;

    private static final String ACCOUNT_NAME = "testAccount";
    private static final String PARTITION_ID = "dataPartitionId";

    @BeforeEach
    void init() {
        initMocks(this);
        lenient().doReturn(ACCOUNT_NAME).when(configuration).getStorageAccountName();
    }

    @Test
    public void should_throwException_given_nullDataPartitionId() {
        try {
            this.sut.getBlobServiceClient(null);
        } catch (NullPointerException ex) {
            assertEquals("dataPartitionId cannot be null!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_throwException_given_emptyDataPartitionId() {
        try {
            this.sut.getBlobServiceClient("");
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
        when(this.blobStoreRetryConfiguration.getRequestRetryOptions()).thenReturn(new RequestRetryOptions());

        BlobServiceClient serviceClient = this.sut.getBlobServiceClient(PARTITION_ID);
        assertNotNull(serviceClient);
    }

    @Test
    public void should_return_cachedContainer_when_cachedEarlier() {
        when(this.partitionService.getPartition(PARTITION_ID)).thenReturn(
                PartitionInfoAzure.builder()
                        .idConfig(Property.builder().value(PARTITION_ID).build())
                        .storageAccountNameConfig(Property.builder().value(ACCOUNT_NAME).build()).build());
        when(this.blobStoreRetryConfiguration.getRequestRetryOptions()).thenReturn(new RequestRetryOptions());

        BlobServiceClient serviceClient = this.sut.getBlobServiceClient(PARTITION_ID);
        assertNotNull(serviceClient);

        final String cacheKey = String.format("%s-blobServiceClient", PARTITION_ID);
        when(this.clientCache.containsKey(cacheKey)).thenReturn(true);
        when(this.clientCache.get(cacheKey)).thenReturn(serviceClient);

        this.sut.getBlobServiceClient(PARTITION_ID);
        verify(this.partitionService, times(1)).getPartition(PARTITION_ID);
    }
}
