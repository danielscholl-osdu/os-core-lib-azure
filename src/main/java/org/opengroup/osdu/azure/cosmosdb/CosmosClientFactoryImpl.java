package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

import com.azure.cosmos.ThrottlingRetryOptions;
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
    @Lazy
    @Autowired
    private PartitionServiceClient partitionService;

    private Map<String, CosmosClient> cosmosClientMap;

    @Lazy
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
     *
     * @param dataPartitionId Data Partition Id
     * @return Cosmos Client Instance
     */
    private CosmosClient createCosmosClient(final String dataPartitionId) {
        PartitionInfoAzure pi = this.partitionService.getPartition(dataPartitionId);

        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        if (cosmosRetryConfiguration.isRetrySupported()) {
            throttlingRetryOptions.setMaxRetryAttemptsOnThrottledRequests(cosmosRetryConfiguration.getRetryCount());
            throttlingRetryOptions.setMaxRetryWaitTime(Duration.ofSeconds(cosmosRetryConfiguration.getRetryWaitTimeout()));
        }

        CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(pi.getCosmosEndpoint())
                .key(pi.getCosmosPrimaryKey())
                .throttlingRetryOptions(throttlingRetryOptions)
                .buildClient();
        CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME)
                .info("Created CosmosClient for dataPartition {}.", dataPartitionId);
        return cosmosClient;
    }
}
