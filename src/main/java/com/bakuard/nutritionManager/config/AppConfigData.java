package com.bakuard.nutritionManager.config;

import java.io.IOException;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Properties;

public class AppConfigData {

    private final MathContext mathContext;
    private final int numberScale;
    private final Properties security;

    public AppConfigData(String configPath, String securityPath) throws IOException {
        Properties config = new Properties();
        config.load(getClass().getResourceAsStream(configPath));
        this.mathContext = new MathContext(
                Integer.parseInt(config.getProperty("decimal.precision")),
                RoundingMode.valueOf(RoundingMode.class, config.getProperty("decimal.rounding"))
        );
        this.numberScale = Integer.parseInt(config.getProperty("decimal.numberScale"));

        security = new Properties();
        security.load(getClass().getResourceAsStream(securityPath));
    }

    public MathContext getMathContext() {
        return mathContext;
    }

    public RoundingMode getRoundingMode() {
        return mathContext.getRoundingMode();
    }

    public int getNumberScale() {
        return numberScale;
    }

    public String getMailServer() {
        return security.getProperty("mail.server");
    }

    public String getMailPassword() {
        return security.getProperty("mail.server.password");
    }

    public String getDatabaseName() {
        return security.getProperty("db.name");
    }

    public String getDatabaseUser() {
        return security.getProperty("db.user");
    }

    public String getDatabasePassword() {
        return security.getProperty("db.password");
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "mathContext=" + mathContext +
                ", numberScale=" + numberScale +
                '}';
    }

}

