package org.opengroup.osdu.azure.httpconfig;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.http.FetchServiceHttpRequest;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.UrlFetchServiceImpl;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

/**
 * Extends URlFetchService and implements IHttpClient to send requests.
 */
@Primary
@Component
public class HttpClientAzure extends UrlFetchServiceImpl implements IHttpClient {

    /**
     * calls urlfetchservice's send request.
     *
     * @param httpRequest made by user class
     * @return HttpResponse
     */
    @Override
    public HttpResponse send(HttpRequest httpRequest) {
        org.opengroup.osdu.core.common.model.http.HttpResponse response = null;
        try {
            response = super.sendRequest(FetchServiceHttpRequest.builder()
                    .body(httpRequest.getBody())
                    .httpMethod(httpRequest.getHttpMethod())
                    .queryParams(httpRequest.getQueryParams())
                    .url(httpRequest.getUrl())
                    .headers(httpRequest.getHeaders())
                    .build());
        } catch (URISyntaxException e) {
            new AppException(HttpStatus.SC_BAD_REQUEST, e.getReason(), "URI Syntax is not correct");
        }
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setBody(response.getBody());
        httpResponse.setResponseCode(response.getResponseCode());
        httpResponse.setContentType(response.getContentType());
        httpResponse.setRequest(httpRequest);
        return httpResponse;
    }
}
