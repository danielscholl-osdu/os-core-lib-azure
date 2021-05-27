package org.opengroup.osdu.azure.entitlements;

import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.common.entitlements.EntitlementsService;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Implements IEntitlementsFactory.
 */
@Component
@Primary
@ConditionalOnProperty(value = "entitlements.factory.azure.enabled", havingValue = "true", matchIfMissing = true)
public class EntitlementsFactoryAzure implements IEntitlementsFactory {

    private final EntitlementsAPIConfig config;
    private final HttpResponseBodyMapper mapper;
    private final IHttpClient client;

    /**
     * Constructor Injection for above 3 fields.
     *
     * @param config EntitlementsAPIConfig
     * @param mapper HttpResponseBodyMapper
     * @param client IHttpClient
     */
    @Autowired
    public EntitlementsFactoryAzure(EntitlementsAPIConfig config, HttpResponseBodyMapper mapper, IHttpClient client) {
        this.config = config;
        this.mapper = mapper;
        this.client = client;
    }

    /**
     * returns instance of EntitlementsService.
     *
     * @param headers DpsHeaders
     * @return IEntitlementsService
     */
    @Override
    public IEntitlementsService create(DpsHeaders headers) {
        Objects.requireNonNull(headers, "headers cannot be null");

        return new EntitlementsService(this.config,
                this.client,
                headers,
                this.mapper);
    }
}
