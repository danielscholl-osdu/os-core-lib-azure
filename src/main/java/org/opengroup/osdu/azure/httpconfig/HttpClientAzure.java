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
// limitations under the License

package org.opengroup.osdu.azure.httpconfig;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.http.FetchServiceHttpRequest;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.UrlFetchServiceImpl;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

/**
 * Extends URlFetchService and implements IHttpClient to send requests.
 */
@Primary
@Component
public class HttpClientAzure implements IHttpClient {

    @Autowired
    private UrlFetchServiceImpl urlFetchService;
    /**
     * calls urlfetchservice's send request.
     *
     * @param httpRequest made by user class
     * @return HttpResponse
     */
    @Override
    public HttpResponse send(final HttpRequest httpRequest) {
        org.opengroup.osdu.core.common.model.http.HttpResponse response = null;
        try {
            response = this.urlFetchService.sendRequest(FetchServiceHttpRequest.builder()
                    .body(httpRequest.getBody())
                    .httpMethod(httpRequest.getHttpMethod())
                    .queryParams(httpRequest.getQueryParams())
                    .url(httpRequest.getUrl())
                    .headers(httpRequest.getHeaders())
                    .build());
        } catch (URISyntaxException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getReason(), "URI Syntax is not correct", e);
        }
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setBody(response.getBody());
        httpResponse.setResponseCode(response.getResponseCode());
        httpResponse.setContentType(response.getContentType());
        httpResponse.setRequest(httpRequest);
        return httpResponse;
    }
}
