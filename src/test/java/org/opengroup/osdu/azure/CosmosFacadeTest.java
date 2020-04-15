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

package org.opengroup.osdu.azure;

import com.azure.cosmos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosmosFacadeTest {

    private static final String ID = "id";
    private static final String PARTITION_KEY = "pk";

    @Mock
    private CosmosContainer container;

    @Mock
    private CosmosItem cosmosItem;

    @Mock
    private CosmosItemProperties cosmosItemProperties;

    @Mock
    private CosmosItemResponse cosmosResponse;

    @Mock
    private Iterator<FeedResponse<CosmosItemProperties>> queryResponse;

    @BeforeEach
    void init() throws CosmosClientException {
        // mock the common cosmos request/response pattern that most tests need. because
        // not all tests will leverage these, we make the mocks lenient.
        lenient().doReturn(cosmosItem).when(container).getItem(ID, PARTITION_KEY);
        lenient().doReturn(cosmosResponse).when(cosmosItem).read(any());
        lenient().doReturn(cosmosItemProperties).when(cosmosResponse).getProperties();
    }

    @Test
    void delete_throws404_ifNotFound() throws CosmosClientException {
        doThrow(NotFoundException.class).when(cosmosItem).delete(any());
        AppException exception = assertThrows(AppException.class, () -> {
            CosmosFacade.deleteItem(container, ID, PARTITION_KEY);
        });
        assertEquals(404, exception.getError().getCode());
    }

    @Test
    void delete_throws500_ifUnknownError() throws CosmosClientException {
        doThrow(CosmosClientException.class).when(cosmosItem).delete(any());
        AppException exception = assertThrows(AppException.class, () -> {
            CosmosFacade.deleteItem(container, ID, PARTITION_KEY);
        });
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void findItem_returnsEmpty_ifNotFound() throws CosmosClientException {
        doThrow(NotFoundException.class).when(cosmosItem).read(any());
        assertFalse(CosmosFacade.findItem(container, ID, PARTITION_KEY, String.class).isPresent());
    }

    @Test
    void findItem_returnsEmpty_ifMalformedDocument() throws IOException {
        doThrow(IOException.class).when(cosmosItemProperties).getObject(any());
        assertFalse(CosmosFacade.findItem(container, ID, PARTITION_KEY, String.class).isPresent());
    }

    @Test
    void findItem_throws500_ifUnknownError() throws CosmosClientException {
        doThrow(CosmosClientException.class).when(cosmosItem).read(any());
        AppException exception = assertThrows(AppException.class, () -> {
            CosmosFacade.findItem(container, ID, PARTITION_KEY, String.class);
        });
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void upsertItem_throws500_ifUnknownError() throws CosmosClientException {
        doThrow(CosmosClientException.class).when(container).upsertItem(any());
        AppException exception = assertThrows(AppException.class, () -> {
            CosmosFacade.upsertItem(container, "some-data");
        });
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void findAllItems_executesCorrectQuery() throws IOException {
        mockQueryResponse("s1");
        CosmosFacade.findAllItems(container, String.class);

        ArgumentCaptor<SqlQuerySpec> query = ArgumentCaptor.forClass(SqlQuerySpec.class);
        ArgumentCaptor<FeedOptions> feedOptions = ArgumentCaptor.forClass(FeedOptions.class);

        verify(container).queryItems(query.capture(), feedOptions.capture());

        assertEquals("SELECT * FROM c", query.getValue().getQueryText());
        assertTrue(feedOptions.getValue().getEnableCrossPartitionQuery());
    }

    @Test
    void findAllItems_pagesCorrectly() throws IOException {
        mockQueryResponse("s1", "s2", "s3");
        List<String> results = CosmosFacade.findAllItems(container, String.class);

        assertEquals(3, results.size());
        assertTrue(results.contains("s1"));
        assertTrue(results.contains("s2"));
        assertTrue(results.contains("s3"));
    }

    private void mockQueryResponse(String... responses) throws IOException {
        ArrayList<FeedResponse<CosmosItemProperties>> paginatedResponse = new ArrayList<>();
        for (String response : responses) {
            @SuppressWarnings("unchecked")
            FeedResponse<CosmosItemProperties> pageResponse = (FeedResponse<CosmosItemProperties>) mock(FeedResponse.class);

            CosmosItemProperties properties = mock(CosmosItemProperties.class);
            doReturn(Collections.singletonList(properties)).when(pageResponse).getResults();
            doReturn(response).when(properties).getObject(any());

            paginatedResponse.add(pageResponse);
        }

        doReturn(paginatedResponse.iterator()).when(container).queryItems(any(SqlQuerySpec.class), any());
    }
}
