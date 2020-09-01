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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.di.BlobStoreConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
public class BlobServiceClientFactoryImplTest {

    @Mock
    DefaultAzureCredential credential;

    @Mock
    BlobStoreConfiguration configuration;

    BlobServiceClientFactoryImpl clientFactory;

    private static final String ACCOUNT_NAME = "testAccount";
    private static final String PARTITION_ID = "dataPartitionId";

    @BeforeEach
    void init() {
        initMocks(this);
        lenient().doReturn(ACCOUNT_NAME).when(configuration).getStorageAccountName();
    }

    @Test
    public void ConstructorThrowsException_IfDefaultAzureCredentialIsNull() {
        try {
            clientFactory = new BlobServiceClientFactoryImpl(null, configuration);
        } catch (NullPointerException ex) {
            assertEquals("Default credentials cannot be null!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void ConstructorThrowsException_IfBlobStoreConfigurationIsNull() {
        doReturn("").when(configuration).getStorageAccountName();

        try {
            clientFactory = new BlobServiceClientFactoryImpl(credential, configuration);
        } catch (IllegalArgumentException ex) {
            assertEquals("Storage account name cannot be null cannot be empty!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void testGetBlobServiceClient_Success()
    {
        try {
            clientFactory = new BlobServiceClientFactoryImpl(credential, configuration);
            BlobServiceClient client = clientFactory.getBlobServiceClient(PARTITION_ID);
            assertEquals(ACCOUNT_NAME, client.getAccountName());
        } catch (Exception ex) {
            fail("Should not fail.");
        }
    }
}
