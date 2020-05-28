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

import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosItem;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.NotFoundException;
import com.azure.cosmos.SqlQuerySpec;
import com.azure.cosmos.internal.AsyncDocumentClient;
import com.azure.cosmos.internal.Document;
import org.opengroup.osdu.core.common.model.http.AppException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simpler interface for interacting with CosmosDB.
 * Usage Examples:
 * <pre>
 * {@code
 *      @Inject
 *      private CosmosContainer container;
 *
 *      void findItemExample() {
 *          Optional<MyObject> myItem = CosmosFacade.findItem(container, "id", "partition-key", MyObject.class);
 *          myItem.isPresent(); // true if found, false otherwise
 *      }
 *
 *      void findAllItemsExample() {
 *          List<MyObject> objects = CosmosFacade.findAllItems(container, MyObject.class);
 *      }
 *
 *      void queryItemsExample() {
 *          SqlQuerySpec query = new SqlQuerySpec()
 *                 .setQueryText("SELECT * FROM c WHERE c.isFoo = @isFoo")
 *                 .setParameters(new SqlParameterList(new SqlParameter("@isFoo", true)));
 *         FeedOptions options = new FeedOptions().setEnableCrossPartitionQuery(true);
 *
 *         List<MyObject> objects = CosmosFacade.queryItems(container, query, options, MyObject.class);
 *      }
 * }
 * </pre>
 */
public final class CosmosFacade {

    private static final Logger LOGGER = Logger.getLogger(CosmosFacade.class.getName());
    /**
     * Private constructor -- this class should never be instantiated.
     */
    private CosmosFacade() {
    }

    /**
     * @param cosmos       Container to query
     * @param id           ID of item
     * @param partitionKey Partition key of item
     */
    public static void deleteItem(
            final CosmosContainer cosmos,
            final String id,
            final String partitionKey) {
        try {
            findItem(cosmos, id, partitionKey).delete(new CosmosItemRequestOptions(partitionKey));
        } catch (NotFoundException e) {
            String errorMessage = "Item was unexpectedly not found";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(404, errorMessage, e.getMessage(), e);
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly failed to delete item from CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    /**
     * @param cosmos       Container to query
     * @param id           ID of item
     * @param partitionKey Partition key of item
     * @param clazz        Class to serialize results into
     * @param <T>          Type to return
     * @return The item that was found based on the IDs provided
     */
    public static <T> Optional<T> findItem(
            final CosmosContainer cosmos,
            final String id,
            final String partitionKey,
            final Class<T> clazz) {
        try {
            T item = findItem(cosmos, id, partitionKey)
                    .read(new CosmosItemRequestOptions(partitionKey))
                    .getProperties()
                    .getObject(clazz);
            return Optional.ofNullable(item);
        } catch (NotFoundException e) {
            LOGGER.info(String.format("Unable to find item with ID=%s and PK=%s", id, partitionKey));
            return Optional.empty();
        } catch (IOException e) {
            LOGGER.warning(String.format("Malformed document for item with ID=%s and PK=%s", id, partitionKey));
            return Optional.empty();
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly encountered error calling CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }
    /**
     * @param container Container to query
     * @param clazz     Class type of response
     * @param <T>       Type of response
     * @return List of items found in container
     */
    public static <T> List<T> findAllItems(final CosmosContainer container, final Class<T> clazz) {
        FeedOptions options = new FeedOptions().setEnableCrossPartitionQuery(true);
        return queryItems(container, new SqlQuerySpec("SELECT * FROM c"), options, clazz);
    }

    /**
     * @param container Container to query
     * @param clazz     Class type of response
     * @param query     {@link SqlQuerySpec} to execute
     * @param options   Query options
     * @param <T>       Type of response
     * @return List of items found in container
     */
    public static <T> List<T> queryItems(final CosmosContainer container, final SqlQuerySpec query, final FeedOptions options, final Class<T> clazz) {
        ArrayList<T> results = new ArrayList<>();
        Iterator<FeedResponse<CosmosItemProperties>> paginatedResponse = container.queryItems(query, options);

        while (paginatedResponse.hasNext()) {
            for (CosmosItemProperties properties : paginatedResponse.next().getResults()) {
                try {
                    results.add(properties.getObject(clazz));
                } catch (IOException e) {
                    String errorMessage = String.format("Malformed document for item with ID=%s", properties.getId());
                    LOGGER.log(Level.WARNING, errorMessage, e);
                    throw new AppException(500, errorMessage, e.getMessage(), e);
                }
            }
        }
        return results;
    }
    /**
     * @param client    {@link AsyncDocumentClient} used to configure/execute requests against database service
     * @param dbName    Cosmos DB name
     * @param container Container to query
     * @param clazz     Class type of response
     * @param <T>       Type of response
     * @param pageSize  Number of items returned
     * @param pageNum   Page number returned
     * @return List of items found on specific page in container
     */
    public static <T> List<T> findAllItems(
            final AsyncDocumentClient client,
            final String dbName,
            final String container,
            final Class<T> clazz,
            final short pageSize,
            final int pageNum) {
        return queryItems(client, dbName, container, new SqlQuerySpec("SELECT * FROM c"), clazz, pageSize, pageNum);
    }

    /**
     * @param client    {@link AsyncDocumentClient} used to configure/execute requests against database service
     * @param dbName    Cosmos DB name
     * @param container Container to query
     * @param query     {@link SqlQuerySpec} to execute
     * @param clazz     Class type of response
     * @param <T>       Type of response
     * @param pageSize  Number of items returned
     * @param pageNum   Page number returned
     * @return List of items found on specific page in container
     */
    public static <T> List<T> queryItems(
            final AsyncDocumentClient client,
            final String dbName,
            final String container,
            final SqlQuerySpec query,
            final Class<T> clazz,
            final short pageSize,
            final int pageNum) {
        String continuationToken = null;
        int currentPage = 0;
        HashMap<String, List<T>> results;
        do {
            String nextContinuationToken = "";

            results = returnItemsWithToken(client, dbName, container, query, clazz, pageSize, continuationToken);
            for (Map.Entry<String, List<T>> entry : results.entrySet()) {
                nextContinuationToken = entry.getKey();
            }
            continuationToken = nextContinuationToken;
            currentPage++;

        } while (currentPage < pageNum && continuationToken != null);
        return results.get(continuationToken);
    }
    /**
     * @param client            {@link AsyncDocumentClient} used to configure/execute requests against database service
     * @param dbName            Cosmos DB name
     * @param container         Container to query
     * @param query             {@link SqlQuerySpec} to execute
     * @param clazz             Class type of response
     * @param <T>               Type of response
     * @param pageSize          Number of items returned
     * @param continuationToken Token used to continue the enumeration
     * @return Continuation Token and list of documents in container
     */
    private static <T> HashMap<String, List<T>> returnItemsWithToken(
            final AsyncDocumentClient client,
            final String dbName,
            final String container,
            final SqlQuerySpec query,
            final Class<T> clazz,
            final short pageSize,
            final String continuationToken) {

        HashMap<String, List<T>> map = new HashMap<>();
        List<T> items = new ArrayList<T>();

        FeedOptions feedOptions = new FeedOptions()
                .maxItemCount((int) pageSize)
                .setEnableCrossPartitionQuery(true)
                .requestContinuation(continuationToken);

        String collectionLink = String.format("/dbs/%s/colls/%s", dbName, container);
        Flux<FeedResponse<Document>> queryFlux = client.queryDocuments(collectionLink, query, feedOptions);

        Iterator<FeedResponse<Document>> it = queryFlux.toIterable().iterator();

        FeedResponse<Document> page = it.next();
        List<Document> results = page.getResults();
        for (Document doc : results) {
            T obj = doc.toObject(clazz);
            items.add(obj);
        }

        map.put(page.getContinuationToken(), items);
        return map;
    }

    /**
     * @param container Container to query
     * @param item      Data object to store
     * @param <T>       Type of response
     */
    public static <T> void upsertItem(final CosmosContainer container, final T item) {
        try {
            container.upsertItem(item);
        } catch (CosmosClientException e) {
            String errorMessage = "Unexpectedly failed to put item into CosmosDB";
            LOGGER.log(Level.WARNING, errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    /**
     * @param cosmos       Container to query
     * @param id           ID of item
     * @param partitionKey Partition key of item
     * @return The item. It may not exist - the caller must check
     */
    private static CosmosItem findItem(
            final CosmosContainer cosmos,
            final String id,
            final String partitionKey) {
        return cosmos.getItem(id, partitionKey);
    }
}
