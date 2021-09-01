package org.opengroup.osdu.azure.legal;

import lombok.Data;
import org.opengroup.osdu.core.common.legal.LegalAPIConfig;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Create Beans for LegalAPIConfig.
 */
@Data
@Component
@ConfigurationProperties
@ConditionalOnProperty(value = "azure.legal.factory.enabled", havingValue = "true", matchIfMissing = true)
public class LegalAPIConfigBean extends AbstractFactoryBean<LegalAPIConfig> {

    private String legalTagApi = "https://os-legal-dot-opendes.appspot.com/api/legal/v1";

    private String legalApiKey = "OBSOLETE";

    /**
     * Abstract Method of AbstractFactoryBean.
     *
     * @return class type of LegalAPIConfig
     */
    @Override
    public Class<?> getObjectType() {
        return LegalAPIConfig.class;
    }

    /**
     * Create instance of LegalAPIConfig.
     *
     * @return LegalAPIConfig
     * @throws Exception
     */
    @Override
    protected LegalAPIConfig createInstance() throws Exception {
        return LegalAPIConfig.builder()
                .apiKey(legalApiKey)
                .rootUrl(legalTagApi)
                .build();
    }
}
