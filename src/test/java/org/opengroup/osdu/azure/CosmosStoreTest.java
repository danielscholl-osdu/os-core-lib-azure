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

import com.azure.cosmos.ConflictException;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosItem;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemResponse;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.NotFoundException;
import com.azure.cosmos.SqlQuerySpec;
import com.azure.cosmos.internal.AsyncDocumentClient;
import com.azure.cosmos.internal.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CosmosStoreTest {

    private static final String ID = "id";
    private static final String PARTITION_KEY = "pk";
    private static final String COSMOS_DB = "cosmosdb";
    private static final String COLLECTION = "collection";
    private static final String COLLECTION_LINK = "/dbs/cosmosdb/colls/collection";
    private static final String DATA_PARTITION_ID = "data-partition-id";

    @Mock
    private AsyncDocumentClient documentClient;

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

    @Mock
    private ICosmosClientFactory cosmosClientFactory;

    @Mock
    private CosmosClient cosmosClient;

    @Mock
    private CosmosDatabase cosmosDatabase;

    @InjectMocks
    private CosmosStore cosmosStore;

    @BeforeEach
    void init() throws CosmosClientException {
        // mock the common cosmos request/response pattern that most tests need. because
        // not all tests will leverage these, we make the mocks lenient.
        lenient().doReturn(cosmosItem).when(container).getItem(ID, PARTITION_KEY);
        lenient().doReturn(cosmosResponse).when(cosmosItem).read(any());
        lenient().doReturn(cosmosItemProperties).when(cosmosResponse).getProperties();
        lenient().doReturn(cosmosClient).when(cosmosClientFactory).getClient(anyString());
        lenient().doReturn(cosmosDatabase).when(cosmosClient).getDatabase(any());
        lenient().doReturn(container).when(cosmosDatabase).getContainer(anyString());
        lenient().doReturn(documentClient).when(cosmosClientFactory).getAsyncClient(anyString());
    }

    @Test
    void delete_throws404_ifNotFound() throws CosmosClientException {
        doThrow(NotFoundException.class).when(cosmosItem).delete(any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.deleteItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY);
        });
        assertEquals(404, exception.getError().getCode());
    }

    @Test
    void delete_throws500_ifUnknownError() throws CosmosClientException {
        doThrow(CosmosClientException.class).when(cosmosItem).delete(any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.deleteItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY);
        });
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void findItem_returnsEmpty_ifNotFound() throws CosmosClientException {
        doThrow(NotFoundException.class).when(cosmosItem).read(any());
        assertFalse(cosmosStore.findItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY, String.class).isPresent());
    }

    @Test
    void findItem_returnsEmpty_ifMalformedDocument() throws IOException {
        doThrow(IOException.class).when(cosmosItemProperties).getObject(any());
        assertFalse(cosmosStore.findItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY, String.class).isPresent());
    }

    @Test
    void findItem_throws500_ifUnknownError() throws CosmosClientException {
        doThrow(CosmosClientException.class).when(cosmosItem).read(any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.findItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY, String.class);
        });
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void upsertItem_throws500_ifUnknownError() throws CosmosClientException {
        doThrow(CosmosClientException.class).when(container).upsertItem(any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.upsertItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, "some-data");
        });
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void createItem_throws409_ifDuplicateDocument() throws CosmosClientException {
        doThrow(ConflictException.class).when(container).createItem(any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.createItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, "some-data");
        });
        assertEquals(409, exception.getError().getCode());
    }

    @Test
    void createItem_throws500_ifUnknownError() throws CosmosClientException {
        doThrow(CosmosClientException.class).when(container).createItem(any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.createItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, "some-data");
        });
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void createItem_Success() throws CosmosClientException {
        try {
            cosmosStore.createItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, "some-data");
        } catch (Exception ex) {
            fail("Should not fail.");
        }
    }

    @Test
    void findAllItems_executesCorrectQuery() throws IOException {
        mockQueryResponse("s1");
        cosmosStore.findAllItems(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, String.class);

        ArgumentCaptor<SqlQuerySpec> query = ArgumentCaptor.forClass(SqlQuerySpec.class);
        ArgumentCaptor<FeedOptions> feedOptions = ArgumentCaptor.forClass(FeedOptions.class);

        verify(container).queryItems(query.capture(), feedOptions.capture());

        assertEquals("SELECT * FROM c", query.getValue().getQueryText());
        assertTrue(feedOptions.getValue().getEnableCrossPartitionQuery());
    }

    @Test
    void findAllItems_pagesCorrectly() throws IOException {
        mockQueryResponse("s1", "s2", "s3");
        List<String> results = cosmosStore.findAllItems(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, String.class);

        assertEquals(3, results.size());
        assertTrue(results.contains("s1"));
        assertTrue(results.contains("s2"));
        assertTrue(results.contains("s3"));
    }

    @Test
    void findAllItems_byPageNumber() {
        mockPaginatedQueryResponse(2, 2, "s1", "s2", "s3", "s4", "s5");

        List<String> results = cosmosStore.findAllItemsAsync(DATA_PARTITION_ID, COSMOS_DB, COLLECTION,
                String.class, (short)2, 2);

        assertEquals(2, results.size());
        assertTrue(results.contains("s3"));
        assertTrue(results.contains("s4"));

        mockPaginatedQueryResponse(3, 2, "T1", "T2", "T3", "T4", "T5");
        results = cosmosStore.findAllItemsAsync(DATA_PARTITION_ID, COSMOS_DB, COLLECTION,
                String.class, (short)3, 2);

        assertEquals(2, results.size());
        assertTrue(results.contains("T4"));
        assertTrue(results.contains("T5"));
    }

    @Test
    void queryItems_byPageNumber() throws IOException {
        mockPaginatedQueryResponse(3, 1, "W1", "W2", "W3", "W4", "W5");
        List<String> results = cosmosStore.queryItemsAsync(DATA_PARTITION_ID, COSMOS_DB, COLLECTION,
                new SqlQuerySpec("SELECT * FROM c"), String.class, (short)3, 1);

        assertEquals(3, results.size());
        assertTrue(results.contains("W1"));
        assertTrue(results.contains("W2"));
        assertTrue(results.contains("W3"));

        mockPaginatedQueryResponse(2, 3, "Z1", "Z2", "Z3", "Z4", "Z5");
        results = cosmosStore.queryItemsAsync(DATA_PARTITION_ID, COSMOS_DB, COLLECTION,
                new SqlQuerySpec("SELECT * FROM c"), String.class, (short)2, 3);

        assertEquals(1, results.size());
        assertTrue(results.contains("Z5"));
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

    private void mockPaginatedQueryResponse(int pageSize, int pageNum, String... responses) {
        List<Document> resp = new ArrayList<>();
        FeedResponse<Document> pageResponse = (FeedResponse<Document>) mock(FeedResponse.class);

        for (String response : responses) {
            Document doc = mock(Document.class);
            resp.add(doc);
            lenient().doReturn(Collections.singletonList(doc)).when(pageResponse).getResults();
            lenient().doReturn(response).when(doc).toObject(any());
        }

        when(pageResponse.getResults()).thenReturn(currentPage(resp,pageSize,pageNum));
        doReturn(Flux.just(pageResponse))
                .when(documentClient)
                .queryDocuments(eq(COLLECTION_LINK), any((SqlQuerySpec.class)), any());
    }

    private static List<Document> currentPage (List<Document> dataList, int pageSize, int pageNum) {
        List<Document> currentPageList = new ArrayList<>();
        if (dataList != null && dataList.size() > 0) {
            int currIdx = (pageNum > 1 ? (pageNum - 1) * pageSize : 0);
            for (int i = 0; i < pageSize && i < dataList.size() - currIdx; i++) {
                currentPageList.add(dataList.get(currIdx + i));
            }
        }
        return currentPageList;
    }
}
