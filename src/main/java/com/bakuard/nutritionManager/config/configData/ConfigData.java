package com.bakuard.nutritionManager.config.configData;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("conf")
@ConstructorBinding
public record ConfigData(Decimal decimal,
                         ConfirmationMail confirmationMail,
                         DataBase database,
                         Aws aws) {}

