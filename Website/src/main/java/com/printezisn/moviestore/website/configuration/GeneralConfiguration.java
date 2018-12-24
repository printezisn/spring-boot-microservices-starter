package com.printezisn.moviestore.website.configuration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.printezisn.moviestore.website.Constants.PageConstants;
import com.printezisn.moviestore.website.configuration.rest.DefaultResponseErrorHandler;

/**
 * General bean configuration class
 */
@Configuration
public class GeneralConfiguration implements WebMvcConfigurer {

    private static final int ASSETS_CACHE_SECONDS = (int) ChronoUnit.SECONDS.between(LocalDateTime.now(),
        LocalDateTime.now().plusMonths(3));

    /**
     * Creates a RestTemplate bean
     * 
     * @param restTemplateBuilder
     *            The RestTemplate builder
     * @return The RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
            .errorHandler(new DefaultResponseErrorHandler())
            .build();
    }

    /**
     * Creates a PageConstants bean
     * 
     * @return The PageConstants bean
     */
    @Bean
    public PageConstants pageConstants() {
        return new PageConstants();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/*.js", "/*.css")
            .addResourceLocations("classpath:/static/dist/")
            .setCachePeriod(ASSETS_CACHE_SECONDS)
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new PathResourceResolver());

        registry
            .addResourceHandler("/fonts/**")
            .addResourceLocations("classpath:/static/dist/fonts/")
            .setCachePeriod(ASSETS_CACHE_SECONDS)
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new PathResourceResolver());

        registry
            .addResourceHandler("/img/**")
            .addResourceLocations("classpath:/static/dist/img/")
            .setCachePeriod(ASSETS_CACHE_SECONDS)
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new PathResourceResolver());
    }
}
