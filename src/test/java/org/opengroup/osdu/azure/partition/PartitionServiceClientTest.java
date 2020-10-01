package org.opengroup.osdu.azure.partition;

import com.azure.security.keyvault.secrets.SecretClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.util.AzureServicePrincipleTokenService;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.partition.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartitionServiceClientTest {

    @Mock
    private SecretClient secretClient;
    @Mock
    private IPartitionFactory partitionFactory;
    @Mock
    private AzureServicePrincipleTokenService tokenService;
    @Mock
    private DpsHeaders headers;
    @InjectMocks
    private PartitionServiceClient sut;

    private static final String PARTITION_ID = "dataPartitionId";

    @Test
    public void should_throwException_given_nullDataPartitionId() {
        try {
            this.sut.getPartition(null);
        } catch (NullPointerException ex) {
            assertEquals("partitionId cannot be null!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_throwException_given_emptyDataPartitionId() {
        try {
            this.sut.getPartition("");
        } catch (IllegalArgumentException ex) {
            assertEquals("partitionId cannot be empty!", ex.getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }

    @Test
    public void should_return_validPartition_given_partitionExists() throws PartitionException {
        PartitionService partitionService = mock(PartitionService.class);
        when(this.tokenService.getAuthorizationToken()).thenReturn("token");
        when(this.partitionFactory.create(this.headers)).thenReturn(partitionService);

        final String storageAccountKey = "testStorageAccountKey";
        final String cosmosEndpoint = "testCosmosEndpoint";
        Map<String, Property> properties = new HashMap<>();
        properties.put("id", Property.builder().value(PARTITION_ID).build());
        properties.put("cosmos-endpoint", Property.builder().value(cosmosEndpoint).build());
        properties.put("storage-account-key", Property.builder().value(storageAccountKey).build());
        PartitionInfo partitionInfo = PartitionInfo.builder().properties(properties).build();
        when(partitionService.get(PARTITION_ID)).thenReturn(partitionInfo);

        PartitionInfoAzure partitionInfoAzure = this.sut.getPartition(PARTITION_ID);
        assertNotNull(partitionInfoAzure);
        assertEquals(cosmosEndpoint, partitionInfoAzure.getCosmosEndpoint());
        assertEquals(storageAccountKey, partitionInfoAzure.getStorageAccountKey());
    }

    @Test
    public void should_throwException_when_partitionNotFound() {
        PartitionService partitionService = mock(PartitionService.class);
        when(this.tokenService.getAuthorizationToken()).thenReturn("token");
        when(this.partitionFactory.create(this.headers)).thenReturn(partitionService);

        try {
            when(partitionService.get(PARTITION_ID)).thenThrow(new PartitionException("unknown error", new HttpResponse()));
            this.sut.getPartition(PARTITION_ID);
        } catch (AppException ex) {
            assertEquals("unknown error", ex.getOriginalException().getMessage());
        } catch (Exception ex) {
            fail("Should not get any other exception. Received " + ex.getClass());
        }
    }
}
