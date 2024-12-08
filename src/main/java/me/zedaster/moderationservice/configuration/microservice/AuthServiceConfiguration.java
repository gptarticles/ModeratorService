package me.zedaster.moderationservice.configuration.microservice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "microservices.auth-service")
@ConfigurationPropertiesScan
public class AuthServiceConfiguration extends MicroserviceConfiguration {
    public AuthServiceConfiguration(String url) {
        super(url);
    }
}
