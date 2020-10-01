package org.opengroup.osdu.azure.di;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * A configuration bean class to set up Azure Active Directory related variables.
 */
@Configuration
@Getter
@Lazy
public class AzureActiveDirectoryConfiguration {

    @Value("${azure.activedirectory.app-resource-id}")
    private String aadClientId;
}