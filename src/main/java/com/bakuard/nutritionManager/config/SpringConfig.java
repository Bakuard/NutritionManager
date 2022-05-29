package com.bakuard.nutritionManager.config;

import com.bakuard.nutritionManager.dal.*;
import com.bakuard.nutritionManager.dal.impl.*;
import com.bakuard.nutritionManager.service.*;
import com.bakuard.nutritionManager.dto.DtoMapper;

import com.bakuard.nutritionManager.service.report.ReportService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import org.flywaydb.core.Flyway;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.servlet.MultipartConfigElement;
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
@EnableScheduling
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
    public DishRepository dishRepository(DataSource dataSource,
                                         AppConfigData appConfiguration,
                                         ProductRepositoryPostgres productRepository) {
        return new DishRepositoryPostgres(dataSource, appConfiguration, productRepository);
    }

    @Bean
    public MenuRepository menuRepository(DataSource dataSource,
                                         AppConfigData appConfiguration,
                                         DishRepositoryPostgres dishRepository,
                                         ProductRepository productRepository) {
        return new MenuRepositoryPostgres(dataSource, appConfiguration, dishRepository, productRepository);
    }

    @Bean
    public UserRepository userRepository(DataSource dataSource) {
        return new UserRepositoryPostgres(dataSource);
    }

    @Bean
    public JwsBlackListRepository jwsBlackListRepository(DataSource dataSource) {
        return new JwsBlackListPostgres(dataSource);
    }

    @Bean
    public ImageRepository imageRepository(DataSource dataSource) {
        return new ImageRepositoryPostgres(dataSource);
    }

    @Bean
    public JwsService jwsService(JwsBlackListRepository jwsBlackListRepository) {
        return new JwsService(jwsBlackListRepository);
    }

    @Bean
    public EmailService emailService(AppConfigData appConfiguration) {
        return new EmailService(appConfiguration);
    }

    @Bean
    public AuthService authService(JwsService jwsService, EmailService emailService, UserRepository userRepository) {
        return new AuthService(jwsService, emailService, userRepository);
    }

    @Bean(initMethod = "loadTemplates")
    public ReportService reportService() {
        return new ReportService();
    }

    @Bean
    public ImageUploaderService imageUploaderService(AppConfigData appConfigData, ImageRepository imageRepository) {
        return new ImageUploaderService(appConfigData, imageRepository);
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofKilobytes(250));
        factory.setMaxRequestSize(DataSize.ofKilobytes(250));
        return factory.createMultipartConfig();
    }

    @Bean
    public DtoMapper dtoMapper(UserRepository userRepository,
                               ProductRepository productRepository,
                               DishRepository dishRepository,
                               MenuRepository menuRepository,
                               MessageSource messageSource,
                               AppConfigData appConfiguration) {
        return new DtoMapper(
                userRepository,
                productRepository,
                dishRepository,
                menuRepository,
                messageSource,
                appConfiguration);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("locales/exceptions", "locales/success");
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
                                version("0.11.0").
                                contact(new Contact().email("purplespicemerchant@gmail.com"))
                );
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").
                allowedOrigins("*").
                allowedMethods("*").
                allowedHeaders("*").
                maxAge(86400);
    }

}
