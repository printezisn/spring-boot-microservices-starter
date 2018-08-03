package com.printezisn.moviestore.website.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "service")
@Getter
@Setter
public class ServiceProperties {
    private String accountServiceUrl;
    private String movieServiceUrl;
}
