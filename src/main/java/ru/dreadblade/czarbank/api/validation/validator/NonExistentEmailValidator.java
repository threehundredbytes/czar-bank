package ru.dreadblade.czarbank.api.validation.validator;

import lombok.RequiredArgsConstructor;
import ru.dreadblade.czarbank.api.validation.constraint.NonExistentEmail;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class NonExistentEmailValidator implements ConstraintValidator<NonExistentEmail, String> {
    private final UserRepository userRepository;

    @Override
    public void initialize(NonExistentEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;
        }

        return !userRepository.existsByEmail(email);
    }
}
