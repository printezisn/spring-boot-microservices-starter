package com.printezisn.moviestore.common.configuration.api;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Contains configuration regarding localization, for REST APIs
 */
@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

	/**
	 * The LocaleResolver bean
	 * 
	 * @return The LocaleResolver bean
	 */
	@Bean
	public LocaleResolver localeResolver() {
		final AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
		localeResolver.setDefaultLocale(Locale.US);
		
		return localeResolver;
	}
	
	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		final ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		
		source.setBasenames("classpath:i18n/messages/messages");
		source.setCacheSeconds(0); 
		source.setDefaultEncoding("UTF-8");
		
		return source;
	} 
}
