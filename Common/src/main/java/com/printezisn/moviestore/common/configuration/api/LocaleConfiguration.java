package com.printezisn.moviestore.common.configuration.api;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Contains configuration regarding localization, for REST APIs
 */
@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

	/**
	 * The LocaleChangeInterceptor bean
	 * 
	 * @return The LocaleChangeInterceptor bean
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		
		return localeChangeInterceptor;
	}
	
	/**
	 * The LocaleResolver bean
	 * 
	 * @return The LocaleResolver bean
	 */
	@Bean
	public LocaleResolver localeResolver() {
		final CookieLocaleResolver localeResolver = new CookieLocaleResolver();
		localeResolver.setDefaultLocale(Locale.US);
		
		return localeResolver;
	}
	
	/**
	 * The ReloadableResourceBundleMessageSource bean 
	 * 
	 * @return The ReloadableResourceBundleMessageSource bean
	 */
	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		final ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		
		source.setBasenames(
			"classpath:i18n/messages/messages",
			"classpath:i18n/pages/pages",
			"classpath:i18n/labels/labels");
		source.setCacheSeconds(0); 
		source.setDefaultEncoding("UTF-8");
		
		return source;
	} 
}
