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
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.policy.RequestRetryOptions;
import org.opengroup.osdu.azure.cache.BlobContainerClientCache;
import org.opengroup.osdu.azure.di.BlobStorageRetryConfiguration;
import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.common.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation for IBlobContainerClientFactory.
 */
@Component
@Lazy
public class BlobContainerClientFactoryImpl implements IBlobContainerClientFactory {

    @Autowired
    private DefaultAzureCredential defaultAzureCredential;

    @Autowired
    private PartitionServiceClient partitionService;

    @Autowired
    private BlobContainerClientCache clientCache;

    @Autowired
    private BlobStorageRetryConfiguration blobStorageRetryConfiguration;

    /**
     * @param dataPartitionId Data partition id
     * @param containerName   Blob container name
     * @return the blob container client instance.
     */
    @Override
    public BlobContainerClient getClient(final String dataPartitionId, final String containerName) {
        Validators.checkNotNull(defaultAzureCredential, "Credential");
        Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");
        Validators.checkNotNullAndNotEmpty(containerName, "containerName");

        String cacheKey = String.format("%s-%s", dataPartitionId, containerName);
        if (this.clientCache.containsKey(cacheKey)) {
            return this.clientCache.get(cacheKey);
        }

        PartitionInfoAzure pi = this.partitionService.getPartition(dataPartitionId);
        String endpoint = String.format("https://%s.blob.core.windows.net", pi.getStorageAccountName());

        RequestRetryOptions requestRetryOptions = blobStorageRetryConfiguration.getRequestRetryOptions();

        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .endpoint(endpoint)
                .credential(defaultAzureCredential)
                .containerName(containerName)
                .retryOptions(requestRetryOptions)
                .buildClient();

        this.clientCache.put(cacheKey, blobContainerClient);

        return blobContainerClient;
    }
}
