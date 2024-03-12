package org.opengroup.osdu.azure.graph;

import com.microsoft.graph.models.odataerrors.ODataError;
import com.microsoft.graph.users.item.UserItemRequestBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/***
 * Simple class to get name via OID from Graph Db.
 */
@Component
@Slf4j
public class GraphService {
    @Autowired
    private GraphServiceClientFactory graphServiceClientFactory;

    /**
     * Checks if an OID is provisioned in Azure Graph.
     * @param dataPartitionId data-partition-id
     * @param oId Object ID
     * @return Name of the user
     */
    public String getByOid(final String dataPartitionId, final String oId) {
        try {
            Validators.checkNotNullAndNotEmpty(oId, "email/OID");
            UserItemRequestBuilder userItemRequestBuilder = graphServiceClientFactory.
                    getGraphServiceClient(dataPartitionId).
                    users().
                    byUserId(oId);

            String userName = userItemRequestBuilder.get().getGivenName();
            log.info("User " + userName + " validated");
            return userName;
        } catch (ODataError error) {
            throw new AppException(error.getResponseStatusCode(), "OID Validation failed", error.getMessage());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new AppException(HttpStatus.SC_FORBIDDEN, "OID Validation failed", exception.getMessage());
        }
    }
}
