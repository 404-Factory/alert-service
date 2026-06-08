package com.factory.alert.validation.constraintvalidators;

import com.factory.alert.validation.constraints.ValidStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class StatusValidator implements ConstraintValidator<ValidStatus, String> {

    private static final Set<String> VALUES = Set.of("READ", "UNREAD");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null 허용 여부는 정책에 따라
        }
        return VALUES.contains(value.toUpperCase());
    }
}