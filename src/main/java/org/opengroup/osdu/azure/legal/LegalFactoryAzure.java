package org.opengroup.osdu.azure.legal;

import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.legal.ILegalFactory;
import org.opengroup.osdu.core.common.legal.ILegalProvider;
import org.opengroup.osdu.core.common.legal.LegalAPIConfig;
import org.opengroup.osdu.core.common.legal.LegalService;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Implements ILegalFactory.
 */
@Component
@Primary
@ConditionalOnProperty(value = "azure.legal.factory.enabled", havingValue = "true", matchIfMissing = true)
public class LegalFactoryAzure implements ILegalFactory {

    private final LegalAPIConfig config;
    private final HttpResponseBodyMapper mapper;
    private final IHttpClient client;

    /**
     * Constructor Injection for following 3 parameter.
     *
     * @param configuration LegalAPIConfig
     * @param bodyMapper    HttpResponseBodyMapper
     * @param httpClient    IHttpClient
     */
    @Autowired
    public LegalFactoryAzure(final LegalAPIConfig configuration, final HttpResponseBodyMapper bodyMapper, final IHttpClient httpClient) {
        this.config = configuration;
        this.mapper = bodyMapper;
        this.client = httpClient;
    }

    /**
     * Creates LegalService.
     *
     * @param dpsHeaders DpsHeaders
     * @return instance of LegalService
     */
    @Override
    public ILegalProvider create(final DpsHeaders dpsHeaders) {
        Objects.requireNonNull(dpsHeaders, "headers cannot be null");
        return new LegalService(this.config,
                this.client,
                dpsHeaders,
                this.mapper);
    }
}
