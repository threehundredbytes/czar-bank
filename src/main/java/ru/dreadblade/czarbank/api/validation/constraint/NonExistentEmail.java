package ru.dreadblade.czarbank.api.validation.constraint;

import ru.dreadblade.czarbank.api.validation.validator.NonExistentEmailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = NonExistentEmailValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface NonExistentEmail {
    String message() default "User with the same email already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
