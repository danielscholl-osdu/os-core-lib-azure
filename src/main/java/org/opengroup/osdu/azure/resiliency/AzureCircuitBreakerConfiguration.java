/** Copyright © Microsoft Corporation

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License
 **/

package org.opengroup.osdu.azure.resiliency;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for CircuitBreaker.
 */
@Configuration
@ConfigurationProperties("azure.circuitbreaker")
@Getter
@Setter
public class AzureCircuitBreakerConfiguration {

    /**
     * Constructor which initializes CircuitBreakerRegistry.
     */
    public AzureCircuitBreakerConfiguration() {
        this.setCBR();
    }

    private boolean enable = false;

    @Getter
    private CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Create CircuitBreakerRegistry.
     */
    private void setCBR() {
        // Create a CircuitBreakerRegistry with a custom global configuration
         this.circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    }
}
