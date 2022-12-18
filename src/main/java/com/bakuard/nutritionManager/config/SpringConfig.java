package com.bakuard.nutritionManager.config;

import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.config.security.RequestContext;
import com.bakuard.nutritionManager.config.security.RequestContextImpl;
import com.bakuard.nutritionManager.dal.*;
import com.bakuard.nutritionManager.dal.impl.*;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.service.AuthService;
import com.bakuard.nutritionManager.service.EmailService;
import com.bakuard.nutritionManager.service.ImageUploaderService;
import com.bakuard.nutritionManager.service.JwsService;
import com.bakuard.nutritionManager.service.menuGenerator.MenuGeneratorService;
import com.bakuard.nutritionManager.service.report.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
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
import java.time.Clock;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class},
        scanBasePackages = {
                "com.bakuard.nutritionManager.controller",
                "com.bakuard.nutritionManager.config"
        }
)
@EnableTransactionManagement
@EnableScheduling
@ConfigurationPropertiesScan
@SecurityScheme(name = "commonToken", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@SecurityScheme(name = "registrationToken", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@SecurityScheme(name = "restorePassToken", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class SpringConfig implements WebMvcConfigurer {

    @Bean
    public DataSource dataSource(ConfigData configData) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        hikariConfig.setUsername(configData.database().user());
        hikariConfig.setPassword(configData.database().password());
        hikariConfig.addDataSourceProperty("databaseName", configData.database().name());
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
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ProductRepository productRepository(DataSource dataSource, ConfigData appConfiguration) {
        return new ProductRepositoryPostgres(dataSource, appConfiguration);
    }

    @Bean
    public DishRepository dishRepository(DataSource dataSource,
                                         ConfigData appConfiguration,
                                         ProductRepositoryPostgres productRepository) {
        return new DishRepositoryPostgres(dataSource, appConfiguration, productRepository);
    }

    @Bean
    public MenuRepository menuRepository(DataSource dataSource,
                                         ConfigData appConfiguration,
                                         DishRepositoryPostgres dishRepository) {
        return new MenuRepositoryPostgres(dataSource, appConfiguration, dishRepository);
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
    public JwsService jwsService(JwsBlackListRepository jwsBlackListRepository,
                                 Clock clock,
                                 ObjectMapper objectMapper) {
        return new JwsService(jwsBlackListRepository, clock, objectMapper);
    }

    @Bean
    public EmailService emailService(ConfigData appConfiguration) {
        return new EmailService(appConfiguration);
    }

    @Bean
    public AuthService authService(JwsService jwsService,
                                   EmailService emailService,
                                   UserRepository userRepository,
                                   ConfigData configData) {
        return new AuthService(jwsService, emailService, userRepository, configData);
    }

    @Bean(initMethod = "loadTemplates")
    public ReportService reportService() {
        return new ReportService();
    }

    @Bean
    public ImageUploaderService imageUploaderService(ConfigData configData, ImageRepository imageRepository) {
        return new ImageUploaderService(configData, imageRepository);
    }

    @Bean
    public MenuGeneratorService menuGeneratorService(ConfigData configData) {
        return new MenuGeneratorService(configData);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
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
                               ConfigData appConfiguration) {
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
    public RequestContext requestContext() {
        return new RequestContextImpl();
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
                                version("0.15.0").
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
