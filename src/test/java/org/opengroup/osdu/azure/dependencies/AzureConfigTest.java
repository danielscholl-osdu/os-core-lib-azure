//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.azure.dependencies;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.CosmosClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AzureConfigTest {
    @Test
    void tokenCredentialIsCorrectType() {
        TokenCredential credential = new AzureOSDUConfig().azureCredential();
        assertNotNull(credential);
    }

    @Test
    void nullAndEmptyChecksAreMade() {
        AzureOSDUConfig config = new AzureOSDUConfig();
        CosmosClient mockClient = Mockito.mock(CosmosClient.class);

        assertThrows(NullPointerException.class, () -> config.cosmosClient(null, "foo"));
        assertThrows(NullPointerException.class, () -> config.cosmosClient("foo", null));
        assertThrows(NullPointerException.class, () -> config.cosmosContainer(null, "foo", "foo"));
        assertThrows(NullPointerException.class, () -> config.cosmosContainer(mockClient, null, "foo"));
        assertThrows(NullPointerException.class, () -> config.cosmosContainer(mockClient, "foo", null));
        assertThrows(NullPointerException.class, () -> config.keyVaultSecretsClient(null, "foo"));
        assertThrows(NullPointerException.class, () -> config.keyVaultSecretsClient(config.azureCredential(), null));
        assertThrows(NullPointerException.class, () -> config.blobContainer(null, "foo", "foo"));
        assertThrows(NullPointerException.class, () -> config.blobContainer(config.azureCredential(), null, "foo"));
        assertThrows(NullPointerException.class, () -> config.blobContainer(config.azureCredential(), "foo", null));
        assertThrows(NullPointerException.class, () -> config.topicClient(null, "foo", "foo"));
        assertThrows(NullPointerException.class, () -> config.topicClient(config.azureCredential(), null, "foo"));
        assertThrows(NullPointerException.class, () -> config.topicClient(config.azureCredential(), "foo", null));

        assertThrows(IllegalArgumentException.class, () -> config.cosmosClient("", "foo"));
        assertThrows(IllegalArgumentException.class, () -> config.cosmosClient("foo", ""));
        assertThrows(IllegalArgumentException.class, () -> config.cosmosContainer(mockClient, "", "foo"));
        assertThrows(IllegalArgumentException.class, () -> config.cosmosContainer(mockClient, "foo", ""));
        assertThrows(IllegalArgumentException.class, () -> config.keyVaultSecretsClient(config.azureCredential(), ""));
        assertThrows(IllegalArgumentException.class, () -> config.blobContainer(config.azureCredential(), "", "foo"));
        assertThrows(IllegalArgumentException.class, () -> config.blobContainer(config.azureCredential(), "foo", ""));
        assertThrows(IllegalArgumentException.class, () -> config.topicClient(config.azureCredential(), "", "foo"));
        assertThrows(IllegalArgumentException.class, () -> config.topicClient(config.azureCredential(), "foo", ""));
    }
}
