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

package org.opengroup.osdu.azure.publisherFacade;

import com.google.gson.Gson;
import com.microsoft.azure.servicebus.Message;
import org.opengroup.osdu.azure.publisherFacade.models.MessageProperties;
import org.opengroup.osdu.azure.publisherFacade.models.PubSubAttributesBuilder;
import org.opengroup.osdu.azure.publisherFacade.models.ServiceBusMessageBody;
import org.opengroup.osdu.azure.servicebus.ITopicClientFactory;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Implementation of Service Bus publisher.
 */
@Component
@ConditionalOnProperty(value = "azure.pubsub.publish", havingValue = "true", matchIfMissing = false)
public class ServiceBusPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusPublisher.class);
    @Autowired
    private ITopicClientFactory topicClientFactory;

    /**
     * @param publisherInfo Contains Service bus batch and publishing details
     * @param headers       DpsHeaders
     */
    public void publishToServiceBus(final DpsHeaders headers, final PublisherInfo publisherInfo) {
        Gson gson = new Gson();
        Message message = new Message();
        // properties
        headers.addCorrelationIdIfMissing();
        PubSubAttributesBuilder pubSubBuilder = PubSubAttributesBuilder.builder().dpsHeaders(headers).build();
        Map<String, Object> properties = pubSubBuilder.createAttributesMap();

        message.setProperties(properties);
        message.setMessageId(publisherInfo.getMessageId());

        // add all to body {"message": {"data":[], "id":...}}
        MessageProperties messageProperties = MessageProperties.builder()
                .data(gson.toJsonTree(publisherInfo.getBatch()))
                .accountId(headers.getPartitionIdWithFallbackToAccountId())
                .partitionId(headers.getPartitionIdWithFallbackToAccountId())
                .correlationId(headers.getCorrelationId())
                .build();
        ServiceBusMessageBody serviceBusMessageBody = ServiceBusMessageBody.builder()
                .message(messageProperties)
                .build();
        message.setBody(gson.toJson(serviceBusMessageBody).toString().getBytes(StandardCharsets.UTF_8));
        message.setContentType("application/json");

        try {
            LOGGER.debug(String.format("Storage publishes message to Service Bus %s", headers.getCorrelationId()));
            topicClientFactory.getClient(headers.getPartitionId(), publisherInfo.getServiceBusTopicName()).send(message);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}


