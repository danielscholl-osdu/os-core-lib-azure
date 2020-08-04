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

import com.azure.storage.blob.BlobContainerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *  Implementation for IBlobContainerClientFactory.
 */
@Component
@Lazy
public class BlobContainerClientFactoryImpl implements IBlobContainerClientFactory {

    @Lazy
    @Autowired
    private BlobContainerClient blobContainerClient;

    /**
     *
     * @param dataPartitionId   Data partition id
     * @return the blob container client instance.
     */
    @Override
    public BlobContainerClient getClient(final String dataPartitionId) {
        return blobContainerClient;
    }
}
