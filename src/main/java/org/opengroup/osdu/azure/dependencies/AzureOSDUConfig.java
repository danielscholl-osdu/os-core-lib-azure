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

package org.opengroup.osdu.azure.dependencies;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.opengroup.osdu.common.Validators;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.inject.Named;

/**
 * Bootstraps Azure dependencies for OSDU.
 */
@Configuration
public class AzureOSDUConfig {

    /**
     * Azure Services that require a credential for access can leverage this
     * {@link DefaultAzureCredential}. This specific implementation will first attempt
     * to authenticate using the following environment
     * variables:
     * AZURE_CLIENT_ID - service principal's app id
     * AZURE_TENANT_ID - id of the principal's Azure Active Directory tenant
     * AZURE_CLIENT_SECRET - one of the service principal's client secrets
     * <p>
     * If this strategy fails, the credential will fallback to using a Managed
     * Identity, if possible, and fail otherwise. This logic is implemented
     * in {@link com.azure.identity.DefaultAzureCredential}.
     *
     * @return A configured credential suitable for authenticating with Azure
     * Services
     */
    @Bean
    @Lazy
    public DefaultAzureCredential azureCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

    /**
     * @param credential  A credential that can be used to authenticate with
     *                    KeyVault
     * @param keyVaultURL The URL of the KeyVault to configure
     * @return A configured KeyVault secret client
     */
    @Bean
    @Lazy
    public SecretClient keyVaultSecretsClient(
            final DefaultAzureCredential credential,
            final @Named("KEY_VAULT_URL") String keyVaultURL) {
        Validators.checkNotNull(credential, "Credential cannot be null");
        Validators.checkNotNullAndNotEmpty(keyVaultURL, "KV URL");
        return new SecretClientBuilder()
                .credential(credential)
                .vaultUrl(keyVaultURL)
                .buildClient();
    }

    /**
     * @param endpoint Cosmos DB Endpoint
     * @param key      Cosmos DB Key
     * @return A client configured to communicate with Cosmos
     */
    @Bean
    @Lazy
    public CosmosClient cosmosClient(
            final @Named("COSMOS_ENDPOINT") String endpoint,
            final @Named("COSMOS_KEY") String key) {
        Validators.checkNotNullAndNotEmpty(endpoint, "Cosmos endpoint");
        Validators.checkNotNullAndNotEmpty(key, "Cosmos key");

        return CosmosClient.builder()
                .setEndpoint(endpoint)
                .setKey(key)
                .buildClient();
    }

    /**
     * @param cosmosClient  An authenticated Cosmos Client
     * @param dbName        The name of the DB in which the container exists
     * @param containerName The container to return
     * @return A client that can communicate with a Cosmos container
     */
    @Bean
    @Lazy
    public CosmosContainer cosmosContainer(
            final CosmosClient cosmosClient,
            final @Named("COSMOS_DB_NAME") String dbName,
            final @Named("COSMOS_CONTAINER_NAME") String containerName) {
        Validators.checkNotNull(cosmosClient, "Cosmos client cannot be null");
        Validators.checkNotNullAndNotEmpty(dbName, "Cosmos DB Name");
        Validators.checkNotNullAndNotEmpty(containerName, "Cosmos container name");
        return cosmosClient.getDatabase(dbName).getContainer(containerName);
    }

    /**
     *
     * @param credential A credential that can be used to authenticate with Storage Account
     * @param storageAccount The name of the Storage Account in which the blob container exists
     * @param containerName The blob container to return
     * @return A client configured to communicate with Storage Account
     */
    @Bean
    @Lazy
    public BlobContainerClient blobContainer(
            final DefaultAzureCredential credential,
            final @Named("STORAGE_ACCOUNT_NAME") String storageAccount,
            final @Named("STORAGE_CONTAINER_NAME") String containerName) {
        Validators.checkNotNull(credential, "Token credential cannot be null");
        Validators.checkNotNullAndNotEmpty(storageAccount, "Storage account name");
        Validators.checkNotNullAndNotEmpty(containerName, "Storage container name");

        String endpoint = String.format("https://%s.blob.core.windows.net", storageAccount);
        return new BlobContainerClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .containerName(containerName)
                .buildClient();
    }

    /**
     * @param defaultCredential  A token credential used to authenticate with Service Bus
     * @param serviceBusName The name of the Service Bus where the topic exists
     * @param topicName Service Bus topic
     * @return A client configured to communicate with a Service Bus Topic
     * @throws ServiceBusException Exception thrown by {@link TopicClient}
     * @throws InterruptedException Exception thrown by {@link TopicClient}
     */
    @Bean
    @Lazy
    public TopicClient topicClient(
            final DefaultAzureCredential defaultCredential,
            final @Named("SERVICE_BUS_NAMESPACE") String serviceBusName,
            final @Named("SERVICE_BUS_TOPIC") String topicName) throws ServiceBusException, InterruptedException {
        Validators.checkNotNull(defaultCredential, "Token credential cannot be null");
        Validators.checkNotNullAndNotEmpty(serviceBusName, "Service bus namespace");
        Validators.checkNotNullAndNotEmpty(topicName, "Service bus topic name");

        final ClientSettings clientSettings = new ClientSettings(
                new DefaultAzureServiceBusCredential(defaultCredential));

        return new TopicClient(serviceBusName, topicName, clientSettings);
    }
}
