package com.factory.alert.validation.constraints;

import com.factory.alert.validation.constraintvalidators.SeverityValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SeverityValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSeverity {

    String message() default "status must be warning, critical or null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}