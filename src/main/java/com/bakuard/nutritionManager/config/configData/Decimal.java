package com.bakuard.nutritionManager.config.configData;

import org.springframework.boot.context.properties.ConstructorBinding;

import java.math.MathContext;
import java.math.RoundingMode;

public record Decimal(MathContext mathContext, int numberScale) {

    @ConstructorBinding
    public Decimal(String roundingMode, String precision, String scale) {
        this(
                new MathContext(
                        Integer.parseInt(precision),
                        RoundingMode.valueOf(roundingMode)
                ),
                Integer.parseInt(scale)
        );
    }

    public RoundingMode roundingMode() {
        return mathContext.getRoundingMode();
    }

}
