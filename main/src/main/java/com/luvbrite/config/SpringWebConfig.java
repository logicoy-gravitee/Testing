package com.luvbrite.config;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@EnableWebMvc
@Configuration
@ComponentScan("com.luvbrite")
@PropertySource("classpath:/env.properties")
public class SpringWebConfig extends WebMvcConfigurerAdapter {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	@PostConstruct
	public void init() {
	    requestMappingHandlerAdapter.setIgnoreDefaultModelOnRedirect(true);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		
		int cachePeriod = 60*60*24*10; //10 days
		
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/").setCachePeriod(cachePeriod);
		registry.addResourceHandler("/.well-known/pki-validation/A1153A35F494DAC02AA4E6735D31C7C3.txt").addResourceLocations("/").setCachePeriod(cachePeriod);
		registry.addResourceHandler("*.txt").addResourceLocations("/").setCachePeriod(cachePeriod);
		registry.addResourceHandler("*.ico").addResourceLocations("/").setCachePeriod(cachePeriod * 10);
	}

	@Bean
	public ResourceBundleMessageSource messageSource() {

		ResourceBundleMessageSource rb = new ResourceBundleMessageSource();
		rb.setBasenames(new String[] { "messages/messages", "messages/validation" });
		return rb;
	}


	@Bean
	public TemplateResolver templateResolver(){
		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setPrefix("/WEB-INF/templates/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("HTML5");
		templateResolver.setOrder(2);
		
		if(env.getProperty("mode").equalsIgnoreCase("dev")){
			templateResolver.setCacheable(false);
		}

		return templateResolver;
	}    

	/**
	 * THYMELEAF: Template Resolver for email templates.
	 */
	@Bean
	public SpringResourceTemplateResolver emailTemplateResolver() {

		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setPrefix("resources/email-templates/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("HTML5");
		templateResolver.setOrder(1);
		
		if(env.getProperty("mode").equalsIgnoreCase("dev")){
			templateResolver.setCacheable(false);
		}

		return templateResolver;
	}


	@Bean
	public SpringTemplateEngine templateEngine(){

		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		final Set<TemplateResolver> templateResolvers = new HashSet<TemplateResolver>();
		templateResolvers.add(templateResolver());
		templateResolvers.add(emailTemplateResolver());
		templateEngine.setTemplateResolvers(templateResolvers);

		return templateEngine;
	}


	@Bean
	public ViewResolver viewResolver(){
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver() ;
		viewResolver.setTemplateEngine(templateEngine());
		viewResolver.setOrder(1);

		return viewResolver;
	}

	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver getCommonsMultipartResolver() {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		multipartResolver.setMaxUploadSize(20971520);   // 20MB
		multipartResolver.setMaxInMemorySize(1048576);  // 1MB
		return multipartResolver;
	}

	@Bean
	public ResourcelessTransactionManager transactionManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean
	public MapJobRepositoryFactoryBean mapJobRepositoryFactory(
			ResourcelessTransactionManager txManager) throws Exception {

		MapJobRepositoryFactoryBean factory = new 
				MapJobRepositoryFactoryBean(txManager);

		factory.afterPropertiesSet();

		return factory;
	}

	@Bean
	public JobRepository jobRepository( MapJobRepositoryFactoryBean factory) throws Exception {
		return factory.getObject();
	}

	@Bean
	public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		return jobLauncher;
	}
}
