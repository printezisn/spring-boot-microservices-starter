package com.printezisn.moviestore.website.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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
		return restTemplateBuilder.build();
	}
}
