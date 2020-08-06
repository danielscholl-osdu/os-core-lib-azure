package org.opengroup.osdu.azure.di;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration bean class to set up CosmosDb variables.
 */
@Configuration
@Getter
public class CosmosDBConfiguration {

    @Value("${tenantInfo.container.name}")
    private String tenantInfoContainer;

    @Value("${azure.cosmosdb.database}")
    private String cosmosDBName;

}
