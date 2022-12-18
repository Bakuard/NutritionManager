package com.bakuard.nutritionManager.config.configData;

import java.time.Duration;

public record Jws(int commonTokenLifeTimeInDays,
                  int registrationTokenLifeTimeInMinutes,
                  int restorePassTokenLifeTimeInMinutes) {

    public Duration commonTokenLifeTime() {
        return Duration.ofDays(commonTokenLifeTimeInDays);
    }

    public Duration registrationTokenLifeTime() {
        return Duration.ofMinutes(registrationTokenLifeTimeInMinutes);
    }

    public Duration restorePassTokenLifeTime() {
        return Duration.ofMinutes(restorePassTokenLifeTimeInMinutes);
    }

}
