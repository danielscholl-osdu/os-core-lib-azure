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

package org.opengroup.osdu.azure.cosmosdb;


import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.logging.CoreLogger;
import org.opengroup.osdu.azure.logging.CoreLoggerFactory;
import org.opengroup.osdu.azure.logging.DependencyLogger;
import org.opengroup.osdu.azure.multitenancy.TenantInfoDoc;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opengroup.osdu.azure.logging.DependencyType.COSMOS_STORE;

@ExtendWith(MockitoExtension.class)
class CosmosStoreTest {

    private static final String ID = "id";
    private static final String PARTITION_KEY = "pk";
    private static final String COSMOS_DB = "cosmosdb";
    private static final String COLLECTION = "collection";
    private static final String COLLECTION_LINK = "/dbs/cosmosdb/colls/collection";
    private static final String DATA_PARTITION_ID = "data-partition-id";
    private static final String ITEM = "ITEM";

    @Mock
    private CoreLoggerFactory coreLoggerFactory;

    @Mock
    private CoreLogger coreLogger;

    @Mock
    private CosmosContainer container;

    @Mock
    private TenantInfoDoc cosmosItem;

    @Mock
    private CosmosItemResponse<TenantInfoDoc> cosmosResponse;


    /*
    @Mock
    private CosmosItemProperties cosmosItemProperties;

    @Mock
    private Iterator<FeedResponse<CosmosItemProperties>> queryResponse;
*/
    @Mock
    private ICosmosClientFactory cosmosClientFactory;

    @Mock
    private CosmosClient cosmosClient;

    @Mock
    private CosmosDatabase cosmosDatabase;

    @Mock
    private DependencyLogger dependencyLogger;

    @InjectMocks
    private CosmosStore cosmosStore;

    /**
     * Workaround for inability to mock static methods like getInstance().
     *
     * @param mock CoreLoggerFactory mock instance
     */
    private void mockSingleton(CoreLoggerFactory mock) {
        try {
            Field instance = CoreLoggerFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, mock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reset workaround for inability to mock static methods like getInstance().
     */
    private void resetSingleton() {
        try {
            Field instance = CoreLoggerFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
            instance.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void init() throws CosmosException {
        mockSingleton(coreLoggerFactory);
        when(coreLoggerFactory.getLogger(anyString())).thenReturn(coreLogger);

        // mock the common cosmos request/response pattern that most tests need. because
        // not all tests will leverage these, we make the mocks lenient.

        lenient().doReturn(cosmosItem).when(cosmosResponse).getItem();
        lenient().doReturn(cosmosResponse).when(container).readItem(any(), any(), any(), any());
        //lenient().doReturn(cosmosItemProperties).when(cosmosResponse).getProperties();
        lenient().doReturn(cosmosClient).when(cosmosClientFactory).getClient(anyString());
        lenient().doReturn(cosmosClient).when(cosmosClientFactory).getSystemClient();
        lenient().doReturn(cosmosDatabase).when(cosmosClient).getDatabase(any());
        lenient().doReturn(container).when(cosmosDatabase).getContainer(anyString());
    }

    @AfterEach
    public void takeDown() {
        resetSingleton();
    }

    @Test
    void delete_throws404_ifNotFound() throws CosmosException {
        doThrow(NotFoundException.class).when(container).deleteItem(any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.deleteItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY);
        });
        assertEquals(404, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("DELETE_ITEM"), eq("id=id partition_key=pk"), eq("cosmosdb/collection"), anyLong(), eq(404), eq(false));
    }

    @Test
    void delete_throws404_ifNotFound_System() throws CosmosException {
        doThrow(NotFoundException.class).when(container).deleteItem(any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.deleteItem(COSMOS_DB, COLLECTION, ID, PARTITION_KEY);
        });
        assertEquals(404, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("DELETE_ITEM"), eq("id=id partition_key=pk"), eq("cosmosdb/collection"), anyLong(), eq(404), eq(false));
    }


    @Test
    void delete_throws500_ifUnknownError() throws CosmosException {
        doThrow(CosmosException.class).when(container).deleteItem(any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.deleteItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY);
        });
        assertEquals(500, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("DELETE_ITEM"), eq("id=id partition_key=pk"), eq("cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void delete_throws500_ifUnknownError_System() throws CosmosException {
        doThrow(CosmosException.class).when(container).deleteItem(any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.deleteItem(COSMOS_DB, COLLECTION, ID, PARTITION_KEY);
        });
        assertEquals(500, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("DELETE_ITEM"), eq("id=id partition_key=pk"), eq("cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void findItem_returnsEmpty_ifNotFound() throws CosmosException {
        doThrow(NotFoundException.class).when(container).readItem(any(), any(), any(), any());
        assertFalse(cosmosStore.findItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY, any(Class.class)).isPresent());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("READ_ITEM"), eq("id=id partition_key=pk"), eq("cosmosdb/collection"), anyLong(), eq(404), eq(false));
    }

    @Test
    void findItem_returnsEmpty_ifNotFound_System() throws CosmosException {
        doThrow(NotFoundException.class).when(container).readItem(any(), any(), any(), any());
        assertFalse(cosmosStore.findItem(COSMOS_DB, COLLECTION, ID, PARTITION_KEY, Object.class).isPresent());
        verify(this.cosmosClientFactory, times(1)).getSystemClient();
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("READ_ITEM"), eq("id=id partition_key=pk"), eq("cosmosdb/collection"), anyLong(), eq(404), eq(false));
    }

    @Test
    void findItem_throws500_ifUnknownError() throws CosmosException {
        doThrow(CosmosException.class).when(container).readItem(any(), any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.findItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY, any(Class.class));
        });
        assertEquals(500, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("READ_ITEM"), eq("id=id partition_key=pk"), eq("cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void findItem_throws500_ifUnknownError_System() throws CosmosException {
        lenient().doThrow(CosmosException.class).when(container).readItem(any(), any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.findItem(COSMOS_DB, COLLECTION, ID, PARTITION_KEY, any(Class.class));
        });
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void upsertItem_throws500_ifUnknownError() throws CosmosException {
        doThrow(CosmosException.class).when(container).upsertItem(any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.upsertItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, "some-data", any());
        });
        assertEquals(500, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("UPSERT_ITEM"), eq("partition_key=some-data"), eq("cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void replaceItem_throws500_ifUnknownError() throws CosmosException {
        ArgumentCaptor<PartitionKey> partitionKeyArgumentCaptor = ArgumentCaptor.forClass(PartitionKey.class);
        doThrow(CosmosException.class).when(container).replaceItem(eq(ITEM), eq(ID), partitionKeyArgumentCaptor.capture(), any(CosmosItemRequestOptions.class));
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.replaceItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY, ITEM);
        });
        assertEquals(500, exception.getError().getCode());
        verify(container).replaceItem(eq(ITEM), eq(ID), any(PartitionKey.class), any(CosmosItemRequestOptions.class));
        Assertions.assertTrue(partitionKeyArgumentCaptor.getValue().toString().contains(PARTITION_KEY));
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("REPLACE_ITEM"), eq("id=id partition_key=pk"), eq("data-partition-id:cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void replaceItem_throws404_ifNotFound() throws CosmosException {
        ArgumentCaptor<PartitionKey> partitionKeyArgumentCaptor = ArgumentCaptor.forClass(PartitionKey.class);
        doThrow(NotFoundException.class).when(container).replaceItem(eq(ITEM), eq(ID), partitionKeyArgumentCaptor.capture(), any(CosmosItemRequestOptions.class));
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.replaceItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY, ITEM);
        });
        assertEquals(404, exception.getError().getCode());
        verify(container).replaceItem(eq(ITEM), eq(ID), any(PartitionKey.class), any(CosmosItemRequestOptions.class));
        Assertions.assertTrue(partitionKeyArgumentCaptor.getValue().toString().contains(PARTITION_KEY));
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("REPLACE_ITEM"), eq("id=id partition_key=pk"), eq("data-partition-id:cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void replaceItem_Success() {
        CosmosItemResponse<String> cosmosItemResponse = mock(CosmosItemResponse.class);
        ArgumentCaptor<PartitionKey> partitionKeyArgumentCaptor = ArgumentCaptor.forClass(PartitionKey.class);
        doReturn(cosmosItemResponse).when(container).replaceItem(eq(ITEM), eq(ID), partitionKeyArgumentCaptor.capture(), any(CosmosItemRequestOptions.class));
        cosmosStore.replaceItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, ID, PARTITION_KEY, ITEM);
        verify(container).replaceItem(eq(ITEM), eq(ID), any(PartitionKey.class), any(CosmosItemRequestOptions.class));
        Assertions.assertTrue(partitionKeyArgumentCaptor.getValue().toString().contains(PARTITION_KEY));
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("REPLACE_ITEM"), eq("id=id partition_key=pk"), eq("data-partition-id:cosmosdb/collection"), anyLong(), eq(200), eq(true));
    }

    @Test
    void createItem_throws409_ifDuplicateDocument() throws CosmosException {
        doThrow(ConflictException.class).when(container).createItem(any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.createItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, "some-data", any());
        });
        assertEquals(409, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("CREATE_ITEM"), eq("partition_key=some-data"), eq("cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void createItem_throws409_ifDuplicateDocument_System() throws CosmosException {
        lenient().doThrow(ConflictException.class).when(container).createItem(any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.createItem(COSMOS_DB, COLLECTION, "some-data", Object.class);
        });
        assertEquals(409, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("CREATE_ITEM"), eq("partition_key=some-data"), eq("cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void createItem_throws500_ifUnknownError() throws CosmosException {
        doThrow(CosmosException.class).when(container).createItem(any(), any(), any());
        AppException exception = assertThrows(AppException.class, () -> {
            cosmosStore.createItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, "some-data", any());
        });
        assertEquals(500, exception.getError().getCode());
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("CREATE_ITEM"), eq("partition_key=some-data"), eq("cosmosdb/collection"), anyLong(), eq(0), eq(false));
    }

    @Test
    void createItem_Success() throws CosmosException {
        try {
            cosmosStore.createItem(DATA_PARTITION_ID, COSMOS_DB, COLLECTION, "some-data", any());
        } catch (Exception ex) {
            fail("Should not fail.");
        }
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("CREATE_ITEM"), eq("partition_key=some-data"), eq("cosmosdb/collection"), anyLong(), eq(200), eq(true));
    }

    @Test
    void createItem_Success_System() throws CosmosException {
        try {
            cosmosStore.createItem(COSMOS_DB, COLLECTION, "some-data", Object.class);
        } catch (Exception ex) {
            fail("Should not fail.");
        }
        verify(dependencyLogger, times(1)).logDependency(eq(COSMOS_STORE), eq("CREATE_ITEM"), eq("partition_key=some-data"), eq("cosmosdb/collection"), anyLong(), eq(200), eq(true));
    }

    /*
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

        List<String> results = cosmosStore.findAllItemsPage(DATA_PARTITION_ID, COSMOS_DB, COLLECTION,
                String.class, (short)2, 2);

        assertEquals(2, results.size());
        assertTrue(results.contains("s3"));
        assertTrue(results.contains("s4"));

        mockPaginatedQueryResponse(3, 2, "T1", "T2", "T3", "T4", "T5");
        results = cosmosStore.findAllItemsPage(DATA_PARTITION_ID, COSMOS_DB, COLLECTION,
                String.class, (short)3, 2);

        assertEquals(2, results.size());
        assertTrue(results.contains("T4"));
        assertTrue(results.contains("T5"));
    }

    @Test
    void queryItems_byPageNumber() throws IOException {
        mockPaginatedQueryResponse(3, 1, "W1", "W2", "W3", "W4", "W5");
        List<String> results = cosmosStore.queryItemsPage(DATA_PARTITION_ID, COSMOS_DB, COLLECTION,
                new SqlQuerySpec("SELECT * FROM c"), String.class, (short)3, 1);

        assertEquals(3, results.size());
        assertTrue(results.contains("W1"));
        assertTrue(results.contains("W2"));
        assertTrue(results.contains("W3"));

        mockPaginatedQueryResponse(2, 3, "Z1", "Z2", "Z3", "Z4", "Z5");
        results = cosmosStore.queryItemsPage(DATA_PARTITION_ID, COSMOS_DB, COLLECTION,
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
    */

}
