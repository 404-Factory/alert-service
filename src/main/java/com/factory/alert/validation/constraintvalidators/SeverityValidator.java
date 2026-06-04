package com.factory.alert.validation.constraintvalidators;

import com.factory.alert.validation.constraints.ValidSeverity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class SeverityValidator implements ConstraintValidator<ValidSeverity, String> {

    private static final Set<String> VALUES = Set.of("WARNING", "CRITICAL");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return VALUES.contains(value.toUpperCase());
    }
}