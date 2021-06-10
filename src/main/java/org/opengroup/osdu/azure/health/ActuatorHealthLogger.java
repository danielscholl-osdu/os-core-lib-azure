package org.opengroup.osdu.azure.health;




import org.opengroup.osdu.azure.logging.CoreLoggerFactory;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.http.ApiVersion;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.boot.actuate.health.HealthEndpointWebExtension;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Implementation for logging features of health check failures.
 * All services can pick up this class from core-lib-azure from the usual dependency that’s added.
 */
@Configuration
public class ActuatorHealthLogger extends HealthEndpointWebExtension {

    private static final String LOGGER_NAME = ActuatorHealthLogger.class.getName();


    /**
     *
     * @param registry the HealthContributorRegistry
     * @param groups the HealthEndpointGroups

     */
    public ActuatorHealthLogger(final HealthContributorRegistry registry, final HealthEndpointGroups groups) {
        super(registry, groups);

        }



    /**
     *
     * @param apiVersion
     * @param securityContext
     * @param showAll
     * @param path
     * @return
     */
    @Override
    public WebEndpointResponse<HealthComponent> health(final ApiVersion apiVersion, final SecurityContext securityContext,
                                                       final boolean showAll, final String... path) {
        WebEndpointResponse<HealthComponent> response = superClassCall(apiVersion, securityContext, showAll, path);
        HealthComponent health = response.getBody();
        if (health == null) {
            return response;
        }

        Status status = health.getStatus();
        if (status != Status.UP) {

            Map<String, HealthComponent> map = ((CompositeHealth) health).getComponents();
            for (String label : map.keySet()) {
                Status componentStatus = map.get(label).getStatus();
                if (componentStatus == Status.DOWN) {
                    CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME).error("Health component {} has status {}", label, componentStatus);
                }
            }
        }
        return response;

    }


    /**
     *
     * @param apiVersion the Api Version
     * @param securityContext the security Context
     * @param showAll the boolean flag
     * @param path the path
     * @return the webEndpointResponse object
     *
     * */
    protected WebEndpointResponse<HealthComponent> superClassCall(final ApiVersion apiVersion, final SecurityContext securityContext, final boolean showAll, final String... path) {
        return super.health(apiVersion, securityContext, showAll, path);
    }

}
