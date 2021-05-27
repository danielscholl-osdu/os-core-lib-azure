package org.opengroup.osdu.azure.entitlements;

import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

/**
 * Creates bean of EntitlementsAPIConfig
 */
@Component
public class EntilementsAPIConfigBean extends AbstractFactoryBean<EntitlementsAPIConfig> {

    @Value("${AUTHORIZE_API}")
    private String AUTHORIZE_API;

    @Value("${AUTHORIZE_API_KEY:}")
    private String AUTHORIZE_API_KEY;

    /**
     * Abstract method of AbstractBeanFactory<T>.
     *
     * @return class type
     */
    @Override
    public Class<?> getObjectType() {
        return EntitlementsAPIConfig.class;
    }

    /**
     * Abstract method of AbstractBeanFactory<T> type.
     *
     * @return EntitlementsAPIConfig
     * @throws Exception
     */
    @Override
    protected EntitlementsAPIConfig createInstance() throws Exception {
        return EntitlementsAPIConfig
                .builder()
                .rootUrl(AUTHORIZE_API)
                .apiKey(AUTHORIZE_API_KEY)
                .build();
    }
}
