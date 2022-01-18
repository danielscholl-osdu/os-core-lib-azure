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

import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.datalakestorage.DataLakeStore;
import org.opengroup.osdu.azure.datalakestorage.IDataLakeClientFactory;
import org.opengroup.osdu.azure.logging.CoreLogger;
import org.opengroup.osdu.azure.logging.CoreLoggerFactory;
import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataLakeStoreTest {

    private static final String PARTITION_ID = "dataPartitionId";
    private static final String DIRECTORY_NAME = "directoryName";
    private static final String FILE_SYSTEM_NAME = "fileSystemName";
    private static final String DESTINATION_FILE_SYSTEM = "destinationSystemName";

    @Mock
    private CoreLoggerFactory coreLoggerFactory;

    @Mock
    private IDataLakeClientFactory dataLakeClientFactory;

    @Mock
    private DataLakeDirectoryClient dataLakeDirectoryClient;

    @InjectMocks
    private DataLakeStore dataLakeStore;

    @InjectMocks
    private PathInfo pathInfo;

    @Mock
    private CoreLogger coreLogger;

    @Captor
    private ArgumentCaptor<DataLakeServiceSasSignatureValues> dataLakeServiceSasSignatureValuesCaptor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        mockSingleton(coreLoggerFactory);
        lenient().when(coreLoggerFactory.getLogger(anyString())).thenReturn(coreLogger);
        lenient().when(dataLakeClientFactory.getDataLakeDirectoryClient(
                PARTITION_ID,DIRECTORY_NAME, FILE_SYSTEM_NAME)).thenReturn(dataLakeDirectoryClient);
    }

    @AfterEach
    public void takeDown() {
        resetSingleton();
    }

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

    @Test
    public void createDirectory_Success(){

        when(dataLakeDirectoryClient.create()).thenReturn(pathInfo);
        dataLakeStore.createDirectory(PARTITION_ID, FILE_SYSTEM_NAME,DIRECTORY_NAME);

        verify(dataLakeClientFactory).getDataLakeDirectoryClient(PARTITION_ID, DIRECTORY_NAME, FILE_SYSTEM_NAME);
        verify(dataLakeDirectoryClient).create();
    }

    @Test
    public void createDirectory_AppException(){
        doThrow(DataLakeStorageException.class).when(dataLakeClientFactory).getDataLakeDirectoryClient(PARTITION_ID,DIRECTORY_NAME, FILE_SYSTEM_NAME);
        try {
            dataLakeStore.createDirectory(PARTITION_ID, FILE_SYSTEM_NAME, DIRECTORY_NAME);
        } catch (AppException exception) {
            assertEquals(500, exception.getError().getCode());
        } catch (Exception exception) {
            fail("should not get different error code");
        }
    }

    @Test
    public void generatePreSignedURL_ReturnsValidSasToken() {
        String containerSasToken = "containerSasToken";
        String containerUrl = "containerUrl";
        String containerPreSignedUrl = String.format("%s?%s",containerUrl,containerSasToken);

        doReturn(containerUrl).when(dataLakeDirectoryClient).getDirectoryUrl();
        doReturn(DIRECTORY_NAME).when(dataLakeDirectoryClient).getDirectoryName();
        doReturn(containerSasToken).when(dataLakeDirectoryClient).generateSas(any(DataLakeServiceSasSignatureValues.class));

        int expiryDays = 1;
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(expiryDays);

        FileSystemSasPermission fileSystemSasPermission =
                (new FileSystemSasPermission()).setReadPermission(true).setCreatePermission(true);

        String obtainedPreSignedUrl = dataLakeStore.generatePreSignedURL(PARTITION_ID, FILE_SYSTEM_NAME, DIRECTORY_NAME,
                expiryTime, fileSystemSasPermission);

        verify(dataLakeDirectoryClient).generateSas(dataLakeServiceSasSignatureValuesCaptor.capture());

        assertEquals(fileSystemSasPermission.toString(), dataLakeServiceSasSignatureValuesCaptor.getValue().getPermissions());
        assertEquals(expiryTime, dataLakeServiceSasSignatureValuesCaptor.getValue().getExpiryTime());
        assertEquals(containerPreSignedUrl, obtainedPreSignedUrl);
    }

    @Test
    public void generatePreSignedURL_ThrowException() {
        doThrow(DataLakeStorageException.class).when(dataLakeDirectoryClient).generateSas(any(DataLakeServiceSasSignatureValues.class));
        int expiryDays = 1;
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(expiryDays);

        FileSystemSasPermission fileSystemSasPermission =
                (new FileSystemSasPermission()).setReadPermission(true).setCreatePermission(true);

        try {
        String obtainedPreSignedUrl = dataLakeStore.generatePreSignedURL(PARTITION_ID, FILE_SYSTEM_NAME, DIRECTORY_NAME,
                expiryTime, fileSystemSasPermission);
        } catch (DataLakeStorageException ex) {
            verify(dataLakeDirectoryClient, times(1)).generateSas(any(DataLakeServiceSasSignatureValues.class));
        } catch (Exception ex) {
            fail("should not get different error code");
        }
    }

    @Test
    public void moveDirectory_success() {
        DataLakeDirectoryClient mockDirectoryClient = mock(DataLakeDirectoryClient.class);
        when(dataLakeDirectoryClient.rename(DESTINATION_FILE_SYSTEM, DIRECTORY_NAME))
                .thenReturn(mockDirectoryClient);

        DataLakeDirectoryClient directoryClient = dataLakeStore.moveDirectory(PARTITION_ID,
                FILE_SYSTEM_NAME, DIRECTORY_NAME, DESTINATION_FILE_SYSTEM);

        assertEquals(mockDirectoryClient, directoryClient);
    }

    @Test
    public void moveDirectory_throwException() {
        doThrow(DataLakeStorageException.class).when(dataLakeDirectoryClient)
                .rename(DESTINATION_FILE_SYSTEM, DIRECTORY_NAME);

        try {
             dataLakeStore.moveDirectory(PARTITION_ID,
                     FILE_SYSTEM_NAME, DIRECTORY_NAME, DESTINATION_FILE_SYSTEM);
        } catch (DataLakeStorageException ex) {
           verify(dataLakeDirectoryClient).rename(DESTINATION_FILE_SYSTEM, DIRECTORY_NAME);
        } catch (Exception ex) {
            fail("should not get different error");
        }
    }

}
