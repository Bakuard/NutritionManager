package com.bakuard.nutritionManager;

import com.bakuard.nutritionManager.config.AppConfigData;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.io.IOException;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class},
        scanBasePackages = {
                "com.bakuard.nutritionManager.controller",
                "com.bakuard.nutritionManager.config"
        }
)
public class SimpleTestConfig {

    @Bean
    public AppConfigData appConfigData(Environment env) throws IOException {
        return new AppConfigData(
                "/config/appConfig.properties",
                "/config/security.properties"
        );
    }

}
