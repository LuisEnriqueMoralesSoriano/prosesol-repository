package com.prosesol.springboot.app.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class CustomMessageConfig {

	@Bean
	public MessageSource messageSource() {
		
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		
		return messageSource;
	}
	
	@Bean
	public LocalValidatorFactoryBean getValidator() {
		
		LocalValidatorFactoryBean localValidator = new LocalValidatorFactoryBean();
		localValidator.setValidationMessageSource(messageSource());
		
		return localValidator;
		
	}
}
