package org.opengroup.osdu.azure.multitenancy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import org.opengroup.osdu.azure.CosmosStore;
import org.opengroup.osdu.azure.di.CosmosDBConfiguration;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

@ExtendWith(MockitoExtension.class)
public class TenantFactoryImplTest {

    private static final String tenantName = "opendes";
    private static final String cosmosDatabase = "cosmos-database";
    private static final String cosmosContainer = "tenant-info";
    private static final String notFound = "not-found";
    private static final String[] groups = {"first"};
    private static final String serviceprincipalAppId = "sp-id";
    private static final String complianceRuleSet = "compliance-rule-set";

    @InjectMocks
    private TenantFactoryImpl tenantFactory;

    @Mock
    private CosmosDBConfiguration cosmosDBConfiguration;

    @Mock
    private CosmosStore cosmosStore;

    @BeforeEach
    public void init() {
        when(cosmosDBConfiguration.getCosmosDBName()).thenReturn(cosmosDatabase);
        when(cosmosDBConfiguration.getTenantInfoContainer()).thenReturn(cosmosContainer);
    }

    @Test
    public void returnsTrueWhenTenantNameFound() {
        TenantInfoDoc doc = new TenantInfoDoc(tenantName, serviceprincipalAppId, complianceRuleSet, groups);

        doReturn(Collections.singletonList(doc)).when(cosmosStore).findAllItems(eq(DpsHeaders.DATA_PARTITION_ID), eq(cosmosDatabase), eq(cosmosContainer), any());
        boolean result = tenantFactory.exists(tenantName);

        assertTrue(result);
    }

    @Test
    public void returnsFalseWhenTenantNameNotFound() {
        TenantInfoDoc doc = new TenantInfoDoc(tenantName + notFound, serviceprincipalAppId, complianceRuleSet, groups);

        doReturn(Collections.singletonList(doc)).when(cosmosStore).findAllItems(eq(DpsHeaders.DATA_PARTITION_ID), eq(cosmosDatabase), eq(cosmosContainer), any());
        boolean result = tenantFactory.exists(tenantName);

        assertFalse(result);
    }

    @Test
    public void returnsTenantInfoObjectWhenTenantNameFound() {
        TenantInfoDoc doc = new TenantInfoDoc(tenantName, serviceprincipalAppId, complianceRuleSet, groups);

        doReturn(Collections.singletonList(doc)).when(cosmosStore).findAllItems(eq(DpsHeaders.DATA_PARTITION_ID), eq(cosmosDatabase), eq(cosmosContainer), any());
        TenantInfo result = tenantFactory.getTenantInfo(tenantName);

        TenantInfo expected = new TenantInfo();
        expected.setName(tenantName);
        expected.setDataPartitionId(tenantName);
        expected.setComplianceRuleSet(complianceRuleSet);
        expected.setServiceAccount(serviceprincipalAppId);
        expected.setCrmAccountIds(Collections.singletonList("first"));

        assertEquals(expected, result);
    }

    @Test
    public void returnsNullWhenTenantNameNotFound() {
        TenantInfoDoc doc = new TenantInfoDoc(tenantName + notFound, serviceprincipalAppId, complianceRuleSet, groups);

        doReturn(Collections.singletonList(doc)).when(cosmosStore).findAllItems(eq(DpsHeaders.DATA_PARTITION_ID), eq(cosmosDatabase), eq(cosmosContainer), any());
        TenantInfo result = tenantFactory.getTenantInfo(tenantName);

        assertNull(result);
    }

    @Test
    public void returnsListOfAllTenants() {
        TenantInfoDoc doc = new TenantInfoDoc(tenantName, serviceprincipalAppId, complianceRuleSet, groups);

        doReturn(Collections.singletonList(doc)).when(cosmosStore).findAllItems(eq(DpsHeaders.DATA_PARTITION_ID), eq(cosmosDatabase), eq(cosmosContainer), any());
        List<TenantInfo> result = new ArrayList<>(tenantFactory.listTenantInfo());

        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setName(tenantName);
        tenantInfo.setDataPartitionId(tenantName);
        tenantInfo.setComplianceRuleSet(complianceRuleSet);
        tenantInfo.setServiceAccount(serviceprincipalAppId);
        tenantInfo.setCrmAccountIds(Collections.singletonList("first"));

        List<TenantInfo> expected = Collections.singletonList(tenantInfo);

        assertEquals(expected, result);
    }
}
