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
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.opengroup.osdu.azure.di.BlobStoreConfiguration;
import org.opengroup.osdu.common.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 *  Implementation for IBlobServiceClientFactory.
 */
@Component
@ConditionalOnProperty(value = "azure.blobStore.required", havingValue = "true", matchIfMissing = false)
public class BlobServiceClientFactoryImpl implements IBlobServiceClientFactory {

    private BlobServiceClient blobServiceClient;

    /**
     * Constructor to initialize blobServiceClient.
     * @param defaultAzureCredential            Default azure credentials.
     * @param blobStoreConfiguration         Configuration details for blob storage.
     */
    @Autowired
    public BlobServiceClientFactoryImpl(
            final DefaultAzureCredential defaultAzureCredential,
            final BlobStoreConfiguration blobStoreConfiguration) {
        Validators.checkNotNull(defaultAzureCredential, "Default credentials");
        Validators.checkNotNullAndNotEmpty(blobStoreConfiguration.getStorageAccountName(), "Storage account name cannot be null");

        String endpoint = String.format("https://%s.blob.core.windows.net", blobStoreConfiguration.getStorageAccountName());
        blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(defaultAzureCredential)
                .buildClient();
    }

    /**
     * @param dataPartitionId data partition id.
     * @return BlobServiceClient corresponding to the given data partition id.
     */
    @Override
    public BlobServiceClient getBlobServiceClient(final String dataPartitionId) {
        return blobServiceClient;
    }
}
