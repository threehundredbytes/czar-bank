package ru.dreadblade.czarbank.api.validation.constraint;

import ru.dreadblade.czarbank.api.validation.validator.PasswordConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ComplexPassword {
    String message() default "The password must contain at least 8 characters, contain at least 1 number, " +
            "1 lowercase and 1 uppercase letter, and a special character (!~<>,;:_=?*+#.\"'&§%°()|[]-$^@/)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
