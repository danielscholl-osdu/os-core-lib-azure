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

package org.opengroup.osdu.azure.multitenancy;

import org.opengroup.osdu.azure.CosmosStore;
import org.opengroup.osdu.azure.di.CosmosDBConfiguration;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * Implementation for ITenantFactory.
 */
@Component
@ConditionalOnProperty(value = "tenantFactoryImpl.required", havingValue = "true", matchIfMissing = false)
public class TenantFactoryImpl implements ITenantFactory {

    @Autowired
    @Lazy
    private CosmosDBConfiguration cosmosDBConfiguration;

    @Autowired
    @Lazy
    private CosmosStore cosmosStore;

    private Map<String, TenantInfo> tenants;

    /**
     * @param tenantName Tenant name
     * @return true or false depending on whether tenant is present
     */
    public boolean exists(final String tenantName) {
        if (this.tenants == null) {
            initTenants();
        }
        return this.tenants.containsKey(tenantName);
    }

    /**
     * @param tenantName Tenant name
     * @return tenantInfo object
     */
    public TenantInfo getTenantInfo(final String tenantName) {
        if (this.tenants == null) {
            initTenants();
        }
        return this.tenants.get(tenantName);
    }

    /**
     * @return list of tenantInfo objects for all the tenants
     */
    public Collection<TenantInfo> listTenantInfo() {
        if (this.tenants == null) {
            initTenants();
        }
        return this.tenants.values();
    }

    /**
     * @param tenantName        Tenant name
     * @param host              Host name
     * @param port              Port
     * @param expireTimeSeconds Expiry time in seconds
     * @param classOfV          Class reference
     * @param <V>               Template class
     * @return cache
     */
    public <V> ICache<String, V> createCache(final String tenantName, final String host, final int port, final int expireTimeSeconds, final Class<V> classOfV) {
        return null;
    }


    /**
     * Flush the cache.
     */
    public void flushCache() {

    }

    /**
     * Initialise the local cache for tenants.
     */
    private void initTenants() {
        this.tenants = new HashMap<>();

        // TODO partition id should not be required because tenant details will be kept in a known partition
        cosmosStore.findAllItems(DpsHeaders.DATA_PARTITION_ID, cosmosDBConfiguration.getCosmosDBName(), cosmosDBConfiguration.getTenantInfoContainer(), TenantInfoDoc.class).
                forEach(tenantInfoDoc -> {
                    TenantInfo ti = new TenantInfo();
                    String tenantName = tenantInfoDoc.getId();
                    ti.setName(tenantName);
                    ti.setComplianceRuleSet(tenantInfoDoc.getComplianceRuleSet());
                    ti.setDataPartitionId(tenantName);
                    this.tenants.put(tenantName, ti);
                }
        );
    }
}