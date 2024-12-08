package me.zedaster.moderationservice.configuration.microservice;

import lombok.Getter;

import java.net.URI;

/**
 * Configuration for a microservice.
 */
@Getter
public abstract class MicroserviceConfiguration {
    private final URI uri;

    public MicroserviceConfiguration(String url) {
        this.uri = URI.create(url);
    }
}
