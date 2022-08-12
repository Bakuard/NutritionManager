package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.*;
import com.bakuard.nutritionManager.dal.impl.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@TestConfiguration
public class DBTestConfig {

    @Bean
    public AppConfigData appConfigData(Environment env) throws IOException {
        return AppConfigData.builder().
                setNumberPrecision(env.getProperty("decimal.precision")).
                setNumberRoundingMod(env.getProperty("decimal.rounding")).
                setNumberScale(env.getProperty("decimal.numberScale")).
                setDatabaseName(env.getProperty("db.name")).
                setDatabaseUser(env.getProperty("db.user")).
                setDatabasePassword(env.getProperty("db.password")).
                build();
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

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
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

}
