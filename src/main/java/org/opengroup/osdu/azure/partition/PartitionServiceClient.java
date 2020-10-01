package org.opengroup.osdu.azure.partition;

import com.azure.security.keyvault.secrets.SecretClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.azure.util.AzureServicePrincipleTokenService;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.partition.IPartitionFactory;
import org.opengroup.osdu.core.common.partition.IPartitionProvider;
import org.opengroup.osdu.core.common.partition.PartitionException;
import org.opengroup.osdu.core.common.partition.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Partition service client implementation.
 */
@Service
@Lazy
public class PartitionServiceClient {

    @Autowired
    private IPartitionFactory partitionFactory;
    @Autowired
    private SecretClient secretClient;
    @Autowired
    private AzureServicePrincipleTokenService tokenService;
    @Autowired
    private DpsHeaders headers;

    private final Gson gson = new Gson();

    /**
     * Get partition info.
     *
     * @param partitionId Partition Id
     * @return Partition info
     * @throws AppException Exception thrown by {@link IPartitionFactory}
     */
    public PartitionInfoAzure getPartition(final String partitionId) throws AppException {
        Validators.checkNotNullAndNotEmpty(partitionId, "partitionId");

        this.headers.put(DpsHeaders.AUTHORIZATION, "Bearer " + this.tokenService.getAuthorizationToken());
        try {
            IPartitionProvider serviceClient = this.partitionFactory.create(headers);
            PartitionInfo partitionInfo = serviceClient.get(partitionId);
            return convert(partitionInfo);
        } catch (PartitionException e) {
            throw new AppException(HttpStatus.SC_FORBIDDEN, "Service unavailable", String.format("Error getting partition info for data-partition: %s", partitionId), e);
        }
    }

    /**
     * @param partitionInfo generic partitionInfo object
     * @return Azure specific partitionInfo
     */
    private PartitionInfoAzure convert(final PartitionInfo partitionInfo) {
        JsonElement jsonElement = gson.toJsonTree(partitionInfo.getProperties());
        PartitionInfoAzure infoAzure = gson.fromJson(jsonElement, PartitionInfoAzure.class);
        infoAzure.configureSecretClient(secretClient);
        return infoAzure;
    }
}