package com.factory.alert.validation.constraints;

import com.factory.alert.validation.constraintvalidators.StatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = StatusValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStatus {

    String message() default "status must be read, unread or null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}