package com.printezisn.moviestore.website.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.printezisn.moviestore.website.Constants.PageConstants;
import com.printezisn.moviestore.website.configuration.rest.DefaultResponseErrorHandler;

/**
 * General bean configuration class
 */
@Configuration
public class GeneralConfiguration {

	/**
	 * Creates a RestTemplate bean
	 * 
	 * @param restTemplateBuilder The RestTemplate builder
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
}
