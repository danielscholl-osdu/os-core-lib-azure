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

package org.opengroup.osdu.azure.di;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * CosmosRetryConfiguration settings.
 */
@Configuration
public class CosmosRetryConfiguration {

    @Value("${azure.blobStore.CustomRetryConfiguration:false}")
    private boolean customRetryApplicable;

    @Value("${azure.cosmos.retryOptions.MaxRetryAttempts:9}")
    private int retryCount;

    @Value("${azure.cosmos.retryOptions.MaxRetryWaitTime:30}")
    private int retryWaitTimeout;

    /**
     *
     * @return retrySupported
     */
    public boolean isCustomRetryApplicable() {
        return customRetryApplicable;
    }

    /**
     *
     * @return retryCount
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     *
     * @return retryWaitTimeout
     */
    public int getRetryWaitTimeout() {
        return retryWaitTimeout;
    }
}
