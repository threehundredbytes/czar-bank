package ru.dreadblade.czarbank.api.validation.validator;

import org.apache.commons.lang3.StringUtils;
import ru.dreadblade.czarbank.api.validation.constraint.ComplexPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ComplexPassword, String> {
    @Override
    public void initialize(ComplexPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true;
        }

        if (StringUtils.isBlank(password) || password.length() < 8) {
            return false;
        }

        return password.matches("(?=.*\\d)(?=.*[!~<>,;:_=?*+#.\"'&§%°()\\|\\[\\]\\-\\$\\^\\@\\/])(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$");
    }
}
