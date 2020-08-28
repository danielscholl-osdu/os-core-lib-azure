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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;;

/**
 * A simpler interface to interact with Azure blob storage.
 * Usage examples:
 * <pre>
 * {@code
 *      @Autowired
 *      private BlobStore blobStore;
 *
 *      String readFromBlobExample()
 *      {
 *          String content = blobStorage.readFromBlob("dataPartitionId", "filePath");
 *             if (content != null)
 *                 return content;
 *      }
 *
 *      void writeToBlobExample()
 *      {
 *          blobStorage.writeToBlob("dataPartitionId", "filePath", "content");
 *      }
 *
 *      void deleteFromBlobExample()
 *      {
 *          Boolean success = blobStorage.deleteFromBlob("dataPartitionId", "filePath");
 *      }
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
@Lazy
public class BlobStore {

    @Autowired
    private IBlobContainerClientFactory blobContainerClientFactory;

    @Autowired
    private IBlobServiceClientFactory blobServiceClientFactory;

    @Autowired
    private ILogger logger;

    private static final String LOG_PREFIX = "azure-core-lib";

    /**
     *
     * @param filePath              Path of file to be read.
     * @param dataPartitionId       Data partition id
     * @return the content of file with provided file path.
     */
    public String readFromBlob(final String dataPartitionId, final String filePath) {
        BlobContainerClient blobContainerClient = getBlobContainerClient(dataPartitionId);
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(filePath).getBlockBlobClient();
        try (ByteArrayOutputStream downloadStream = new ByteArrayOutputStream()) {
            blockBlobClient.download(downloadStream);
            return downloadStream.toString(StandardCharsets.UTF_8.name());
        } catch (BlobStorageException ex) {
            if (ex.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                String errorMessage = "Specified blob was not found";
                logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
                throw new AppException(404, errorMessage, ex.getMessage(), ex);
            } else {
                String errorMessage = "Failed to read specified blob";
                logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
                throw new AppException(500, errorMessage, ex.getMessage(), ex);
            }
        } catch (UnsupportedEncodingException ex) {
            String errorMessage = String.format("Encoding was not correct for item with name=%s", filePath);
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(400, errorMessage, ex.getMessage(), ex);
        } catch (IOException ex) {
            String errorMessage = String.format("Malformed document for item with name=%s", filePath);
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(500, errorMessage, ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param filePath              Path of file to be deleted.
     * @param dataPartitionId       Data partition id
     * @return boolean indicating whether the deletion of given file was successful or not.
     */
    public boolean deleteFromBlob(final String dataPartitionId, final String filePath) {
        BlobContainerClient blobContainerClient = getBlobContainerClient(dataPartitionId);
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(filePath).getBlockBlobClient();
        try {
            blockBlobClient.delete();
            return true;
        } catch (BlobStorageException ex) {
            if (ex.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                String errorMessage = "Specified blob was not found";
                logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
                throw new AppException(404, errorMessage, ex.getMessage(), ex);
            } else {
                String errorMessage = "Failed to delete blob";
                logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
                throw new AppException(500, errorMessage, ex.getMessage(), ex);
            }
        }
    }

    /**
     *
     * @param filePath              Path of file to be written at.
     * @param content               Content to be written in the file.
     * @param dataPartitionId       Data partition id
     */
    public void writeToBlob(final String dataPartitionId,
                            final String filePath,
                            final String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        int bytesSize = bytes.length;
        BlobContainerClient blobContainerClient = getBlobContainerClient(dataPartitionId);
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(filePath).getBlockBlobClient();
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(bytes)) {
            blockBlobClient.upload(dataStream, bytesSize, true);
        } catch (BlobStorageException ex) {
            String errorMessage = "Failed to upload file content.";
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(500, errorMessage, ex.getMessage(), ex);
        } catch (IOException ex) {
            String errorMessage = String.format("Malformed document for item with name=%s", filePath);
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(500, errorMessage, ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param filePath              Path of file to be read.
     * @param dataPartitionId       Data partition id
     * @param containerName         Name of the storage container
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
                String errorMessage = "Specified blob was not found";
                logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
                throw new AppException(404, errorMessage, ex.getMessage(), ex);
            } else {
                String errorMessage = "Failed to read specified blob";
                logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
                throw new AppException(500, errorMessage, ex.getMessage(), ex);
            }
        } catch (UnsupportedEncodingException ex) {
            String errorMessage = String.format("Encoding was not correct for item with name=%s", filePath);
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(400, errorMessage, ex.getMessage(), ex);
        } catch (IOException ex) {
            String errorMessage = String.format("Malformed document for item with name=%s", filePath);
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(500, errorMessage, ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param filePath              Path of file to be deleted.
     * @param dataPartitionId       Data partition id
     * @param containerName         Name of the storage container
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
                String errorMessage = "Specified blob was not found";
                logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
                throw new AppException(404, errorMessage, ex.getMessage(), ex);
            } else {
                String errorMessage = "Failed to delete blob";
                logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
                throw new AppException(500, errorMessage, ex.getMessage(), ex);
            }
        }
    }

    /**
     *
     * @param filePath              Path of file to be written at.
     * @param content               Content to be written in the file.
     * @param dataPartitionId       Data partition id
     * @param containerName         Name of the storage container
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
            String errorMessage = "Failed to upload file content.";
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(500, errorMessage, ex.getMessage(), ex);
        } catch (IOException ex) {
            String errorMessage = String.format("Malformed document for item with name=%s", filePath);
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(500, errorMessage, ex.getMessage(), ex);
        }
    }


    /**
     *
     * @param dataPartitionId       Data partition id
     * @return blob container client corresponding to the dataPartitionId.
     */
    private BlobContainerClient getBlobContainerClient(final String dataPartitionId) {
        try {
            return blobContainerClientFactory.getClient(dataPartitionId);
        } catch (Exception ex) {
            String errorMessage = "Error creating creating blob container client.";
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(500, errorMessage, ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param dataPartitionId       Data partition id.
     * @param containerName         Name of storage container.
     * @return blob container client corresponding to the dataPartitionId.
     */
    private BlobContainerClient getBlobContainerClient(final String dataPartitionId, final String containerName) {
        try {
            BlobServiceClient serviceClient = blobServiceClientFactory.getBlobServiceClient(dataPartitionId);
            return serviceClient.getBlobContainerClient(containerName);
        } catch (Exception ex) {
            String errorMessage = "Error creating creating blob container client.";
            logger.warning(LOG_PREFIX, errorMessage, Collections.<String, String>emptyMap());
            throw new AppException(500, errorMessage, ex.getMessage(), ex);
        }
    }

}
