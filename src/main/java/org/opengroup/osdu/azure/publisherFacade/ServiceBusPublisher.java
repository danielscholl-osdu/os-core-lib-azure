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
import com.google.gson.JsonObject;
import com.microsoft.azure.servicebus.Message;
import org.opengroup.osdu.azure.servicebus.ITopicClientFactory;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Service Bus publisher.
 */
@Component
@ConditionalOnProperty(value = "azure.serviceBus.enabled", havingValue = "true", matchIfMissing = false)
public class ServiceBusPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusPublisher.class);
    @Autowired
    private ITopicClientFactory topicClientFactory;
    @Autowired
    private PubsubConfiguration pubsubConfiguration;

    /**
     * @param publisherInfo Contains Service bus batch and publishing details
     * @param headers       DpsHeaders
     */
    public void publishToServiceBus(final DpsHeaders headers, final PublisherInfo publisherInfo) {
        Gson gson = new Gson();
        Message message = new Message();
        Map<String, Object> properties = new HashMap<>();

        // properties
        properties.put(DpsHeaders.ACCOUNT_ID, headers.getPartitionIdWithFallbackToAccountId());
        properties.put(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionIdWithFallbackToAccountId());
        headers.addCorrelationIdIfMissing();
        properties.put(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
        message.setProperties(properties);

        // add all to body {"message": {"data":[], "id":...}}
        JsonObject jo = new JsonObject();
        jo.add("data", gson.toJsonTree(publisherInfo.getBatch()));
        jo.addProperty(DpsHeaders.ACCOUNT_ID, headers.getPartitionIdWithFallbackToAccountId());
        jo.addProperty(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionIdWithFallbackToAccountId());
        jo.addProperty(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
        JsonObject jomsg = new JsonObject();
        jomsg.add("message", jo);

        message.setBody(jomsg.toString().getBytes(StandardCharsets.UTF_8));
        message.setContentType("application/json");

        try {
            LOGGER.debug(String.format("Storage publishes message to Service Bus %s", headers.getCorrelationId()));
            topicClientFactory.getClient(headers.getPartitionId(), publisherInfo.getServiceBusTopicName()).send(message);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}


