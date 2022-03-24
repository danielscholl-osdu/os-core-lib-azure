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

import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.azure.servicebus.TopicClientFactoryImpl;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServiceBusPublisherTest {
    private static final String DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID = "data-partition-account-id";
    private static final String CORRELATION_ID = "correlation-id";
    private static final String PARTITION_ID = "partition-id";
    private static final String SERVICE_BUS_TOPIC_NAME = "recordstopic";
    private static final String MESSAGE_ID = "message-id";
    @Mock
    private DpsHeaders dpsHeaders;
    @Mock
    private Object batch;
    @Mock
    private TopicClient topicClient;
    @Mock
    private JaxRsDpsLog logger;
    @Mock
    private Message message;
    @Mock
    private TopicClientFactoryImpl topicClientFactory;
    @Mock
    private PublisherInfo publisherInfo;
    @InjectMocks
    private ServiceBusPublisher sut;

    @BeforeEach
    public void init() throws ServiceBusException, InterruptedException {
        initMocks(this);

        lenient().doReturn(DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID).when(dpsHeaders).getPartitionIdWithFallbackToAccountId();
        lenient().doReturn(PARTITION_ID).when(dpsHeaders).getPartitionId();
        lenient().doReturn(CORRELATION_ID).when(dpsHeaders).getCorrelationId();
        lenient().doReturn(SERVICE_BUS_TOPIC_NAME).when(publisherInfo).getServiceBusTopicName();
        lenient().doReturn(batch).when(publisherInfo).getBatch();
        lenient().doReturn(topicClient).when(topicClientFactory).getClient(any(), any());
        lenient().doReturn(MESSAGE_ID).when(publisherInfo).getMessageId();
    }

    @Test
    public void shouldPublishToServiceBus() {
        try {
            doReturn(topicClient).when(topicClientFactory).getClient(PARTITION_ID, SERVICE_BUS_TOPIC_NAME);
            doNothing().when(topicClient).send(message);
            sut.publishToServiceBus(dpsHeaders, publisherInfo);
            verify(topicClientFactory, times(1)).getClient(PARTITION_ID, SERVICE_BUS_TOPIC_NAME);
        } catch (Exception e) {
            fail("Should not get any exception. Received " + e.getClass());
        }
    }

    @Test
    public void shouldThrowExceptionWhenPublishToServiceBusFails() {
        try {
            doThrow(new Exception()).when(topicClientFactory).getClient(PARTITION_ID, SERVICE_BUS_TOPIC_NAME);
            sut.publishToServiceBus(dpsHeaders, publisherInfo);
            fail("Should throw exception");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

}