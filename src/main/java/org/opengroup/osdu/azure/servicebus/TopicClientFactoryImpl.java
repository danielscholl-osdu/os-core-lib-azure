package org.opengroup.osdu.azure.servicebus;

import com.azure.identity.DefaultAzureCredential;
import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.opengroup.osdu.azure.cache.TopicClientCache;
import org.opengroup.osdu.azure.dependencies.DefaultAzureServiceBusCredential;
import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.common.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation for ITopicClientFactory.
 */
@Component
@Lazy
public class TopicClientFactoryImpl implements ITopicClientFactory {

    @Autowired
    private DefaultAzureCredential defaultAzureCredential;

    @Autowired
    private PartitionServiceClient partitionService;

    @Autowired
    private TopicClientCache clientCache;

    /**
     * @param dataPartitionId Data Partition Id
     * @param topicName       Service Bus topic
     * @return A client configured to communicate with a Service Bus Topic
     * @throws ServiceBusException  Exception thrown by {@link TopicClient}
     * @throws InterruptedException Exception thrown by {@link TopicClient}
     */
    @Override
    public TopicClient getClient(final String dataPartitionId, final String topicName) throws ServiceBusException, InterruptedException {
        Validators.checkNotNull(defaultAzureCredential, "Credential");
        Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");
        Validators.checkNotNullAndNotEmpty(topicName, "topicName");

        String cacheKey = String.format("%s-%s", dataPartitionId, topicName);
        if (this.clientCache.containsKey(cacheKey)) {
            return this.clientCache.get(cacheKey);
        }

        PartitionInfoAzure pi = this.partitionService.getPartition(dataPartitionId);
        final ClientSettings clientSettings = new ClientSettings(
                new DefaultAzureServiceBusCredential(defaultAzureCredential));

        TopicClient topicClient = new TopicClient(pi.getSbNamespace(), topicName, clientSettings);

        this.clientCache.put(cacheKey, topicClient);

        return topicClient;
    }
}