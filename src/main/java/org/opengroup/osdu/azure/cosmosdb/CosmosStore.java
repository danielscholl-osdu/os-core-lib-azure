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

package org.opengroup.osdu.azure.cosmosdb;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.opengroup.osdu.azure.query.CosmosStorePageRequest;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * A simpler interface for interacting with CosmosDB.
 * Usage Examples:
 * <pre>
 * {@code
 *      @Inject
 *      private CosmosContainer container;
 *
 *      @Inject
 *      private CosmosStore cosmosStore;
 *
 *      void findItemExample() {
 *          Optional<MyObject> myItem = cosmosStore.findItem("dataPartitionId", "cosmosDb", "collection", "id", "partition-key", MyObject.class);
 *          myItem.isPresent(); // true if found, false otherwise
 *      }
 *
 *      void findAllItemsExample() {
 *          List<MyObject> objects = cosmosStore.findAllItems("dataPartitionId", "cosmosDb", "collection", MyObject.class);
 *      }
 *
 *      void queryItemsExample() {
 *          SqlQuerySpec query = new SqlQuerySpec()
 *                 .setQueryText("SELECT * FROM c WHERE c.isFoo = @isFoo")
 *                 .setParameters(new SqlParameterList(new SqlParameter("@isFoo", true)));
 *         FeedOptions options = new FeedOptions().setEnableCrossPartitionQuery(true);
 *
 *         List<MyObject> objects = cosmosStore.queryItems("dataPartitionId", "cosmosDb", "collection", query, options, MyObject.class);
 *      }
 *
 *      void createItemExample() {
 *          cosmosStore.createItem("dataPartitionId", "cosmosDb", "collection", "some-data");
 *      }
 * }
 * </pre>
 */

@Component
@Lazy
public class CosmosStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosStore.class.getName());
    private static final int PREFERRED_PAGE_SIZE = 1000;

    @Autowired
    private ICosmosClientFactory cosmosClientFactory;

    /**
     *
     * @param dataPartitionId Data partition id
     * @param cosmosDBName Database name
     * @param collection Collection name
     * @param id ID of item
     * @param partitionKey Partition key of item
     * @param clazz Class to serialize results into
     * @param <T> Type to return
     * @return The item
     */
    public <T> Optional<T> findItem(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final String id,
            final String partitionKey,
            final Class<T> clazz) {
        try {
            CosmosContainer container = getCosmosContainer(dataPartitionId, cosmosDBName, collection);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            PartitionKey key = new PartitionKey(partitionKey);
            T item = container.readItem(id, key, options, clazz).getItem();
            return Optional.ofNullable((T) item);
        } catch (NotFoundException e) {
            LOGGER.warn(String.format("Unable to find item with ID=%s and PK=%s", id, partitionKey));
            return Optional.empty();
        } catch (CosmosException e) {
            String errorMessage;
            errorMessage = "Unexpectedly encountered error calling CosmosDB";
            LOGGER.warn(errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    /**
     *
     * @param dataPartitionId Data partition id
     * @param cosmosDBName Database name
     * @param collection Collection name
     * @param id ID of item
     * @param partitionKey Partition key of item
     * @param <T> Type of item
     */
    public <T> void deleteItem(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final String id,
            final String partitionKey) {
        try {
            CosmosContainer container = getCosmosContainer(dataPartitionId, cosmosDBName, collection);
            PartitionKey key = new PartitionKey(partitionKey);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            container.deleteItem(id, key, options);
        } catch (NotFoundException e) {
            String errorMessage = "Item was unexpectedly not found";
            LOGGER.warn(errorMessage, e);
            throw new AppException(404, errorMessage, e.getMessage(), e);
        } catch (CosmosException e) {
            String errorMessage = "Unexpectedly failed to delete item from CosmosDB";
            LOGGER.warn(errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    /**
     *
     * @param dataPartitionId Data partition id
     * @param cosmosDBName  Database name
     * @param collection Collection name
     * @param partitionKey Partition key of item
     * @param item Data object to store
     * @param <T> Type of item
     */
    public <T> void upsertItem(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final String partitionKey,
            final T item) {
        try {
            CosmosContainer cosmosContainer = getCosmosContainer(dataPartitionId, cosmosDBName, collection);
            PartitionKey key = new PartitionKey(partitionKey);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            cosmosContainer.upsertItem(item, key, options);
        } catch (CosmosException e) {
            String errorMessage = "Unexpectedly failed to put item into CosmosDB";
            LOGGER.warn(errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    /**
     * @param dataPartitionId Data partition id
     * @param cosmosDBName    Database name
     * @param collection      Collection name
     * @param id              ID of item
     * @param partitionKey    Partition key of item
     * @param item            Data object to store
     * @param <T>             Type of item
     */
    public <T> void replaceItem(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final String id,
            final String partitionKey,
            final T item) {
        try {
            CosmosContainer cosmosContainer = getCosmosContainer(dataPartitionId, cosmosDBName, collection);
            PartitionKey key = new PartitionKey(partitionKey);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            cosmosContainer.replaceItem(item, id, key, options);
        } catch (NotFoundException e) {
            String errorMessage = "Item was unexpectedly not found";
            LOGGER.warn(errorMessage, e);
            throw new AppException(404, errorMessage, e.getMessage(), e);
        } catch (CosmosException e) {
            String errorMessage = "Unexpectedly failed to replace item into CosmosDB";
            LOGGER.warn(errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    /**
     *
     * @param dataPartitionId Data partition id
     * @param cosmosDBName Database name
     * @param collection Collection name
     * @param partitionKey Partition key of item
     * @param item  Data object to store
     * @param <T> Type of item
     */
    public <T> void createItem(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final String partitionKey,
            final T item) {
        try {
            CosmosContainer cosmosContainer = getCosmosContainer(dataPartitionId, cosmosDBName, collection);
            PartitionKey key = new PartitionKey(partitionKey);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            cosmosContainer.createItem(item, key, options);
        } catch (ConflictException e) {
            String errorMessage = "Resource with specified id or name already exists.";
            LOGGER.warn(errorMessage, e);
            throw new AppException(409, errorMessage, e.getMessage(), e);
        } catch (CosmosException e) {
            String errorMessage = "Unexpectedly failed to insert item into CosmosDB";
            LOGGER.warn(errorMessage, e);
            throw new AppException(500, errorMessage, e.getMessage(), e);
        }
    }

    // Find All and Queries

    /**
     *
     * @param dataPartitionId dataPartitionId
     * @param cosmosDBName Database name
     * @param collection Collection name
     * @param clazz Class type of response
     * @param  <T> Type
     * @return  List<T> List of items found
     */
    public <T> List<T> findAllItems(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final Class<T> clazz) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        return queryItems(dataPartitionId, cosmosDBName, collection, new SqlQuerySpec("SELECT * FROM c"), options, clazz);
    }

    /**
     * @param dataPartitionId Data partition id
     * @param cosmosDBName Database name
     * @param collection Collection name
     * @param query {@link SqlQuerySpec} to execute
     * @param options Options
     * @param clazz Class type of response
     * @param <T> Type
     * @return List<T> List of items found on specific page in container
     */
    public <T> List<T> queryItems(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final SqlQuerySpec query,
            final CosmosQueryRequestOptions options,
            final Class<T> clazz) {
        List<T> results = new ArrayList<>();
        CosmosContainer cosmosContainer = getCosmosContainer(dataPartitionId, cosmosDBName, collection);
        CosmosPagedIterable<T> paginatedResponse = cosmosContainer.queryItems(query, options, clazz);
        paginatedResponse.iterableByPage(PREFERRED_PAGE_SIZE).forEach(cosmosItemPropertiesFeedResponse -> {
            LOGGER.info("Got a page of query result with {} items(s)",
                    cosmosItemPropertiesFeedResponse.getResults().size());
            results.addAll(cosmosItemPropertiesFeedResponse.getResults());
        });
        LOGGER.info("Done. Retrieved {} results", results.size());
        return results;
    }

    /**
     *
     * @param dataPartitionId Data partition id
     * @param cosmosDBName Database
     * @param collection Collection
     * @param clazz Class type of response
     * @param pageSize Page size
     * @param continuationToken Continuation token
     * @param <T> Type of items
     * @return Page<T> Page of items
     */
    public <T> Page<T> findAllItemsPage(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final Class<T> clazz,
            final int pageSize,
            final String continuationToken) {
        return queryItemsPage(dataPartitionId, cosmosDBName, collection, new SqlQuerySpec("SELECT * FROM c"), clazz, pageSize, continuationToken);
    }

    /**
     *
     * @param dataPartitionId Data partition id
     * @param cosmosDBName Database name
     * @param collection Collection name
     * @param query {@link SqlQuerySpec} to execute
     * @param clazz Class type
     * @param pageSize Page size
     * @param continuationToken Continuation token
     * @param <T> Type
     * @return Page<T> Page of itemns found
     */
    public <T> Page<T> queryItemsPage(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection,
            final SqlQuerySpec query,
            final Class<T> clazz,
            final int pageSize,
            final String continuationToken) {

        int currentPageNumber = 1;
        int iterationNumber = 1;
        int documentNumber = 0;

        String internalcontinuationToken = continuationToken;
        List<T> results = new ArrayList<>();
        CosmosContainer container = getCosmosContainer(dataPartitionId, cosmosDBName, collection);

        LOGGER.info("Receiving a set of query response pages.");
        LOGGER.info("Continuation Token: " + internalcontinuationToken + "\n");

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

        Iterable<FeedResponse<T>> feedResponseIterator =
                container.queryItems(query, queryOptions, clazz).iterableByPage(internalcontinuationToken, pageSize);

        Iterator<FeedResponse<T>> iterator = feedResponseIterator.iterator();
        if (iterator.hasNext()) {
            FeedResponse<T> page = feedResponseIterator.iterator().next();
            LOGGER.info(String.format("Current page number: %d", currentPageNumber));
            // Access all of the documents in this result page
            for (T item : page.getResults()) {
                documentNumber++;
                results.add(item);
            }

            // Page count so far
            LOGGER.info(String.format("Total documents received so far: %d", documentNumber));

            // Along with page results, get a continuation token
            // which enables the client to "pick up where it left off"
            // in accessing query response pages.
            internalcontinuationToken = page.getContinuationToken();
            currentPageNumber++;
            iterationNumber++;
        }

        LOGGER.info("Done. Retrieved {} results", results.size());
        CosmosStorePageRequest pageRequest = CosmosStorePageRequest.of(currentPageNumber, pageSize, internalcontinuationToken);
        return new PageImpl(results, pageRequest, documentNumber);
    }

    /**
     * @param dataPartitionId Data partition id
     * @param cosmosDBName    Database name
     * @param collection      Collection name
     * @return Cosmos container
     */
    private CosmosContainer getCosmosContainer(
            final String dataPartitionId,
            final String cosmosDBName,
            final String collection) {
        try {
            return cosmosClientFactory.getClient(dataPartitionId)
                    .getDatabase(cosmosDBName)
                    .getContainer(collection);
        } catch (AppException ae) {
            throw this.handleCosmosStoreException(ae.getError().getCode(), "Error creating creating Cosmos Client", ae);
        } catch (Exception e) {
            throw this.handleCosmosStoreException(500, "Error creating creating Cosmos Client", e);
        }
    }

    /**
     * Logs and returns instance of AppException.
     *
     * @param status       Response status code
     * @param errorMessage Error message
     * @param e            Original exception
     * @return Instance of AppException
     */
    private AppException handleCosmosStoreException(final int status, final String errorMessage, final Exception e) {
        LOGGER.warn(errorMessage, e);
        return new AppException(status, errorMessage, e.getMessage(), e);
    }
}
