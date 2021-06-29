package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.security.keyvault.secrets.SecretClient;
import org.opengroup.osdu.azure.KeyVaultFacade;
import org.opengroup.osdu.azure.cosmosdb.system.config.SystemCosmosConfig;
import org.opengroup.osdu.azure.logging.CoreLoggerFactory;
import org.opengroup.osdu.azure.di.CosmosRetryConfiguration;
import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.common.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation for ICosmosClientFactory.
 */
@Component
@Lazy
public class CosmosClientFactoryImpl implements ICosmosClientFactory {
    private static final String LOGGER_NAME = CosmosClientFactoryImpl.class.getName();
    private static final String SYSTEM_COSMOS_CACHE_KEY = "system_cosmos";

    @Lazy
    @Autowired
    private PartitionServiceClient partitionService;

    @Autowired
    private SecretClient secretClient;

    @Autowired
    private SystemCosmosConfig systemCosmosConfig;

    private Map<String, CosmosClient> cosmosClientMap;

    @Autowired
    private CosmosRetryConfiguration cosmosRetryConfiguration;

    /**
     * Initializes the private variables as required.
     */
    @PostConstruct
    public void initialize() {
        cosmosClientMap = new ConcurrentHashMap<>();
    }

    /**
     * @param dataPartitionId Data Partition Id
     * @return Cosmos Client instance
     */
    @Override
    public CosmosClient getClient(final String dataPartitionId) {
        Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");

        String cacheKey = String.format("%s-cosmosClient", dataPartitionId);
        if (this.cosmosClientMap.containsKey(cacheKey)) {
            return this.cosmosClientMap.get(cacheKey);
        }

        return this.cosmosClientMap.computeIfAbsent(cacheKey, cosmosClient -> createCosmosClient(dataPartitionId));
    }

    /**
     * @return Cosmos client instance for system resources.
     */
    @Override
    public CosmosClient getSystemClient() {

        if (this.cosmosClientMap.containsKey(SYSTEM_COSMOS_CACHE_KEY)) {
            return this.cosmosClientMap.get(SYSTEM_COSMOS_CACHE_KEY);
        }
        return this.cosmosClientMap.computeIfAbsent(
                SYSTEM_COSMOS_CACHE_KEY, cosmosClient -> createSystemCosmosClient()
        );
    }

    /**
     *
     * @param dataPartitionId Data Partition Id
     * @return Cosmos Client Instance
     */
    private CosmosClient createCosmosClient(final String dataPartitionId) {
        PartitionInfoAzure pi = this.partitionService.getPartition(dataPartitionId);

        ThrottlingRetryOptions throttlingRetryOptions = cosmosRetryConfiguration.getThrottlingRetryOptions();

        CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(pi.getCosmosEndpoint())
                .key(pi.getCosmosPrimaryKey())
                .throttlingRetryOptions(throttlingRetryOptions)
                .buildClient();
        CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME)
                .info("Created CosmosClient for dataPartition {}.", dataPartitionId);
        return cosmosClient;
    }

    /**
     * Method to create the cosmos client for system resources.
     * @return cosmos client.
     */
    private CosmosClient createSystemCosmosClient() {
        CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(getSecret(systemCosmosConfig.getCosmosDBAccountKeyName()))
                .key(getSecret(systemCosmosConfig.getCosmosPrimaryKeyName()))
                .buildClient();
        CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME)
                .info("Created CosmosClient for system resources");

        return cosmosClient;
    }

    /**
     * @param keyName Name of the key to be read from key vault.
     * @return secret value
     */
    private String getSecret(final String keyName) {
        return KeyVaultFacade.getSecretWithValidation(secretClient, keyName);
    }
}
