package com.printezisn.moviestore.movieservice.global.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Class with configuration for the connection with elasticsearch
 */
@Configuration
public class ElasticsearchConfiguration {

	@Value("${elasticsearch.indexName}")
	private String elasticSearchIndexName;
	
	/**
	 * Returns the name of the index that is used in elasticsearch
	 * 
	 * @return The name of the index
	 */
	@Bean
	public String elasticSearchIndexName() {
		return elasticSearchIndexName;
	}
}
