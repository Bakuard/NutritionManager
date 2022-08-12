package com.bakuard.nutritionManager.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import java.math.MathContext;
import java.math.RoundingMode;

public class AppConfigData {

    public static Builder builder() {
        return new Builder();
    }


    private final MathContext mathContext;
    private final int numberScale;
    private final String mailServer;
    private final String mailPassword;
    private final String databaseName;
    private final String databaseUser;
    private final String databasePassword;
    private final String awsUserId;
    private final AWSStaticCredentialsProvider credentialsProvider;

    private AppConfigData(String numberPrecision,
                          String numberRoundingMod,
                          String numberScale,
                          String mailServer,
                          String mailPassword,
                          String databaseName,
                          String databaseUser,
                          String databasePassword,
                          String awsUserId,
                          String awsAccessKey,
                          String awsSecretKey) {
        //Because AWS resources use DNS name entries that occasionally change
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");

        if(numberPrecision != null && numberRoundingMod != null) {
            this.mathContext = new MathContext(
                    Integer.parseInt(numberPrecision),
                    RoundingMode.valueOf(numberRoundingMod)
            );
        } else {
            this.mathContext = null;
        }

        if(numberScale != null) this.numberScale = Integer.parseInt(numberScale);
        else this.numberScale = 0;

        this.mailServer = mailServer;
        this.mailPassword = mailPassword;
        this.databaseName = databaseName;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.awsUserId = awsUserId;

        if(awsAccessKey != null && awsSecretKey != null) {
            this.credentialsProvider = new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(awsAccessKey, awsSecretKey)
            );
        } else {
            this.credentialsProvider = null;
        }
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
        return mailServer;
    }

    public String getMailPassword() {
        return mailPassword;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public AWSStaticCredentialsProvider getAwsCredentialsProvider() {
        return credentialsProvider;
    }

    public String getAwsUserId() {
        return awsUserId;
    }

    @Override
    public String toString() {
        return "AppConfigData{" +
                "mathContext=" + mathContext +
                ", numberScale=" + numberScale +
                ", mailServer='" + mailServer + '\'' +
                ", mailPassword='" + mailPassword + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", databaseUser='" + databaseUser + '\'' +
                ", databasePassword='" + databasePassword + '\'' +
                ", awsUserId='" + awsUserId + '\'' +
                ", credentialsProvider=" + credentialsProvider +
                '}';
    }


    public static class Builder {

        private String numberPrecision;
        private String numberRoundingMod;
        private String numberScale;
        private String mailServer;
        private String mailPassword;
        private String databaseName;
        private String databaseUser;
        private String databasePassword;
        private String awsUserId;
        private String awsAccessKey;
        private String awsSecretKey;

        private Builder() {

        }

        public Builder setNumberPrecision(String numberPrecision) {
            this.numberPrecision = numberPrecision;
            return this;
        }

        public Builder setNumberRoundingMod(String numberRoundingMod) {
            this.numberRoundingMod = numberRoundingMod;
            return this;
        }

        public Builder setNumberScale(String numberScale) {
            this.numberScale = numberScale;
            return this;
        }

        public Builder setMailServer(String mailServer) {
            this.mailServer = mailServer;
            return this;
        }

        public Builder setMailPassword(String mailPassword) {
            this.mailPassword = mailPassword;
            return this;
        }

        public Builder setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder setDatabaseUser(String databaseUser) {
            this.databaseUser = databaseUser;
            return this;
        }

        public Builder setDatabasePassword(String databasePassword) {
            this.databasePassword = databasePassword;
            return this;
        }

        public Builder setAwsUserId(String awsUserId) {
            this.awsUserId = awsUserId;
            return this;
        }

        public Builder setAwsAccessKey(String awsAccessKey) {
            this.awsAccessKey = awsAccessKey;
            return this;
        }

        public Builder setAwsSecretKey(String awsSecretKey) {
            this.awsSecretKey = awsSecretKey;
            return this;
        }

        public AppConfigData build() {
            return new AppConfigData(
                    numberPrecision,
                    numberRoundingMod,
                    numberScale,
                    mailServer,
                    mailPassword,
                    databaseName,
                    databaseUser,
                    databasePassword,
                    awsUserId,
                    awsAccessKey,
                    awsSecretKey
            );
        }

    }

}

