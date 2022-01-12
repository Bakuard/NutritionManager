package com.bakuard.nutritionManager.config;

import com.bakuard.nutritionManager.dal.*;
import com.bakuard.nutritionManager.dal.impl.*;
import com.bakuard.nutritionManager.services.*;
import com.bakuard.nutritionManager.dto.DtoMapper;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.flywaydb.core.Flyway;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.sql.DataSource;
import java.io.IOException;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class},
        scanBasePackages = {
                "com.bakuard.nutritionManager.controller",
                "com.bakuard.nutritionManager.config"
        }
)
@EnableTransactionManagement
public class SpringConfig implements WebMvcConfigurer {

    @Bean
    public AppConfigData appConfigData() throws IOException {
        return new AppConfigData(
                "/config/appConfig.properties",
                "/config/security.properties"
        );
    }

    @Bean
    public DataSource dataSource(AppConfigData appConfigData) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        hikariConfig.setUsername(appConfigData.getDatabaseUser());
        hikariConfig.setPassword(appConfigData.getDatabasePassword());
        hikariConfig.addDataSourceProperty("databaseName", appConfigData.getDatabaseName());
        hikariConfig.setAutoCommit(false);
        hikariConfig.addDataSourceProperty("portNumber", "5432");
        hikariConfig.addDataSourceProperty("serverName", "localhost");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setPoolName("hikariPool");

        return new HikariDataSource(hikariConfig);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure().
                locations("classpath:db").
                dataSource(dataSource).
                load();
    }

    @Bean("transactionManager")
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ProductRepository productRepository(DataSource dataSource, AppConfigData appConfiguration) {
        return new ProductRepositoryPostgres(dataSource, appConfiguration);
    }

    @Bean
    public DishRepository dishRepository(DataSource dataSource) {
        return new DishRepositoryPostgres(dataSource);
    }

    @Bean
    public MenuRepository menuRepository(DataSource dataSource) {
        return new MenuRepositoryPostgres(dataSource);
    }

    @Bean
    public UserRepository userRepository(DataSource dataSource) {
        return new UserRepositoryPostgres(dataSource);
    }

    @Bean
    public JwsService jwsService() {
        return new JwsService();
    }

    @Bean
    public EmailService emailService(AppConfigData appConfiguration) {
        return new EmailService(appConfiguration);
    }

    @Bean
    public AuthService authService(JwsService jwsService, EmailService emailService, UserRepository userRepository) {
        return new AuthService(jwsService, emailService, userRepository);
    }

    @Bean
    public DtoMapper dtoMapper(UserRepository userRepository,
                               MessageSource messageSource,
                               AppConfigData appConfiguration) {
        return new DtoMapper(userRepository, messageSource, appConfiguration);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("locales/exceptions");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
    }


    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().
                info(
                        new Info().
                                title("Nutrition Manager API").
                                version("0.2.0").
                                contact(new Contact().email("purplespicemerchant@gmail.com"))
                );
    }

}
