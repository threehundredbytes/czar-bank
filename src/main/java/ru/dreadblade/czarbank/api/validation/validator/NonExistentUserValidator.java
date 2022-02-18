package ru.dreadblade.czarbank.api.validation.validator;

import lombok.RequiredArgsConstructor;
import ru.dreadblade.czarbank.api.validation.constraint.NonExistentUser;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class NonExistentUserValidator implements ConstraintValidator<NonExistentUser, String> {
    private final UserRepository userRepository;
    
    @Override
    public void initialize(NonExistentUser constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null) {
            return true;
        }
        
        return !userRepository.existsByUsername(username);
    }
}