package org.opengroup.osdu.azure.httpconfig;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.opengroup.osdu.azure.di.RetryAndTimeoutsConfiguration;
import org.opengroup.osdu.core.common.http.HttpClientHandler;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Extends HttpClientHandler.
 */
@Primary
@Component
public class HttpClientHandlerAzure extends HttpClientHandler {

    @Autowired
    private RetryAndTimeoutsConfiguration configuration;

    /**
     * Constructor to set request and timeout configuration of HttpClientHandler.
     */
    public HttpClientHandlerAzure() {
        super.REQUEST_CONFIG = RequestConfig.custom()
                .setConnectTimeout(configuration.getConnectTimeoutInMillis())
                .setConnectionRequestTimeout(configuration.getConnectTimeoutInMillis())
                .setSocketTimeout(configuration.getSocketTimeout()).build();

        super.RETRY_COUNT = configuration.getRetryCountForServiceUnavailableStrategy();
    }

    /**
     * same as the HttpClientHandler's send request.
     *
     * @param request  HttpRequestBase
     * @param requestHeaders DpsHeaders
     * @return HttpResponse
     */
    public HttpResponse sendRequest(HttpRequestBase request, DpsHeaders requestHeaders) {
        return super.sendRequest(request, requestHeaders);
    }
}
