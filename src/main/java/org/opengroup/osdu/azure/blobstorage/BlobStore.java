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

package org.opengroup.osdu.azure.blobstorage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * A simpler interface to interact with Azure blob storage.
 * Usage examples:
 * <pre>
 * {@code
 *      @Autowired
 *      private BlobStore blobStore;
 *
 *      String readFromStorageContainerExamole()
 *      {
 *          String content = blobStorage.readFromStorageContainer("dataPartitionId", "filePath", "containerName");
 *          if (content != null)
 *                 return content;
 *      }
 *
 *      void writeToStorageContainerExample()
 *      {
 *          blobStorage.writeToStorageContainer("dataPartitionId", "filePath", "content", "containerName");
 *      }
 *
 *      void deleteFromStorageContainerExample()
 *      {
 *          Boolean success = blobStorage.deleteFromStorageContainer("dataPartitionId", "filePath", "containerName");
 *      }
 * }
 * </pre>
 */

@Component
@ConditionalOnProperty(value = "azure.blobStore.required", havingValue = "true", matchIfMissing = false)
public class BlobStore {

    @Autowired
    private IBlobServiceClientFactory blobServiceClientFactory;

    @Autowired
    private ILogger logger;

    private static final String LOG_PREFIX = "azure-core-lib";

    /**
     * @param filePath        Path of file to be read.
     * @param dataPartitionId Data partition id
     * @param containerName   Name of the storage container
     * @return the content of file with provided file path.
     */
    public String readFromStorageContainer(
            final String dataPartitionId,
            final String filePath,
            final String containerName) {
        BlobContainerClient blobContainerClient = getBlobContainerClient(dataPartitionId, containerName);
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(filePath).getBlockBlobClient();
        try (ByteArrayOutputStream downloadStream = new ByteArrayOutputStream()) {
            blockBlobClient.download(downloadStream);
            return downloadStream.toString(StandardCharsets.UTF_8.name());
        } catch (BlobStorageException ex) {
            if (ex.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                throw handleBlobStoreException(404, "Specified blob was not found", ex);
            }
            throw handleBlobStoreException(500, "Failed to read specified blob", ex);
        } catch (UnsupportedEncodingException ex) {
            throw handleBlobStoreException(400, String.format("Encoding was not correct for item with name=%s", filePath), ex);
        } catch (IOException ex) {

            throw handleBlobStoreException(500, String.format("Malformed document for item with name=%s", filePath), ex);
        }
    }

    /**
     * @param filePath        Path of file to be deleted.
     * @param dataPartitionId Data partition id
     * @param containerName   Name of the storage container
     * @return boolean indicating whether the deletion of given file was successful or not.
     */
    public boolean deleteFromStorageContainer(
            final String dataPartitionId,
            final String filePath,
            final String containerName) {
        BlobContainerClient blobContainerClient = getBlobContainerClient(dataPartitionId, containerName);
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(filePath).getBlockBlobClient();
        try {
            blockBlobClient.delete();
            return true;
        } catch (BlobStorageException ex) {
            if (ex.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                throw handleBlobStoreException(404, "Specified blob was not found", ex);
            }
            throw handleBlobStoreException(500, "Failed to delete blob", ex);
        }
    }

    /**
     * @param filePath        Path of file to be written at.
     * @param content         Content to be written in the file.
     * @param dataPartitionId Data partition id
     * @param containerName   Name of the storage container
     */
    public void writeToStorageContainer(
            final String dataPartitionId,
            final String filePath,
            final String content,
            final String containerName) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        int bytesSize = bytes.length;
        BlobContainerClient blobContainerClient = getBlobContainerClient(dataPartitionId, containerName);
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(filePath).getBlockBlobClient();
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(bytes)) {
            blockBlobClient.upload(dataStream, bytesSize, true);
        } catch (BlobStorageException ex) {
            throw handleBlobStoreException(500, "Failed to upload file content.", ex);
        } catch (IOException ex) {
            throw handleBlobStoreException(500, String.format("Malformed document for item with name=%s", filePath), ex);
        }
    }

    /**
     * @param dataPartitionId Data partition id.
     * @param containerName   Name of storage container.
     * @return blob container client corresponding to the dataPartitionId.
     */
    private BlobContainerClient getBlobContainerClient(final String dataPartitionId, final String containerName) {
        try {
            BlobServiceClient serviceClient = blobServiceClientFactory.getBlobServiceClient(dataPartitionId);
            return serviceClient.getBlobContainerClient(containerName);
        } catch (AppException ex) {
            throw handleBlobStoreException(ex.getError().getCode(), "Error creating creating blob container client.", ex);
        } catch (Exception ex) {
            throw handleBlobStoreException(500, "Error creating creating blob container client.", ex);
        }
    }

    /**
     * Logs and returns instance of AppException.
     *
     * @param status       Response status code
     * @param errorMessage Error message
     * @param ex           Original exception
     * @return Instance of AppException
     */
    private AppException handleBlobStoreException(final int status, final String errorMessage, final Exception ex) {
        logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
        return new AppException(status, errorMessage, ex.getMessage(), ex);
    }
}
