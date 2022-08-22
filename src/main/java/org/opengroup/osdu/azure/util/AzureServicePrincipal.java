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

package org.opengroup.osdu.azure.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *  Class to generate the AAD authentication tokens.
 */
public final class AzureServicePrincipal {

    private static final String ERROR_REASON = "invalid_request";
    private static final String ERROR_MESSAGE = "Passing invalid parameters or endpoints to a method getIdToken";

    /**
     * @param sp_id             AZURE CLIENT ID
     * @param sp_secret         AZURE CLIENT SECRET
     * @param tenant_id         AZURE TENANT ID
     * @param app_resource_id   AZURE APP RESOURCE ID
     * @return                  AUTHENTICATION TOKEN
     * @throws UnsupportedEncodingException        throws UnsupportedEncodingException
     */
    public String getIdToken(final String sp_id, final String sp_secret, final String tenant_id, final String app_resource_id) throws UnsupportedEncodingException {

        String aadEndpoint = String.format("https://login.microsoftonline.com/%s/oauth2/token", tenant_id);
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, aadEndpoint);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("grant_type", "client_credentials");
        parameters.put("client_id", sp_id);
        parameters.put("client_secret", sp_secret);
        parameters.put("resource", app_resource_id);
        httpRequest.setBody(getParamsString(parameters));

        HttpClient client = createHttpClient();

        Mono<HttpResponse> response = client.send(httpRequest);
        HttpResponse httpResponse = Objects.requireNonNull(response.block());
        String content = httpResponse.getBodyAsString().block();

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(content, JsonObject.class);

        if (httpResponse.getStatusCode() == 200) {
            return jsonObject.get("access_token").getAsString();
        } else {
            String error = Optional.ofNullable(jsonObject).isPresent() && Optional.ofNullable(jsonObject.get("error")).isPresent() ? jsonObject.get("error").getAsString() : ERROR_REASON;
            String errorDescription = Optional.ofNullable(jsonObject).isPresent() && Optional.ofNullable(jsonObject.get("error_description")).isPresent() ? jsonObject.get("error_description").getAsString() : ERROR_MESSAGE;
            throw new AppException(httpResponse.getStatusCode(), error, errorDescription);
        }
    }

    /**
     * Method to generate MSI tokens.
     * @return AUTHENTICATION TOKEN
     */
    public String getMSIToken() {

        String aadEndpoint = "http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01&resource=https://management.azure.com/";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, aadEndpoint);
        httpRequest.setHeader("Metadata", "true");

        HttpClient client = createHttpClient();

        Mono<HttpResponse> response = client.send(httpRequest);
        String content = Objects.requireNonNull(response.block()).getBodyAsString().block();

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
        return jsonObject.get("access_token").getAsString();
    }

    /**
     * @param params    Map of request parameters
     * @return          parameter string
     * @throws UnsupportedEncodingException throws exception unsupported encoding is found
     */
    private String getParamsString(final Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    /**
     * @return HttpClient
     */
    HttpClient createHttpClient() {
        return new NettyAsyncHttpClientBuilder().build();
    }
}
