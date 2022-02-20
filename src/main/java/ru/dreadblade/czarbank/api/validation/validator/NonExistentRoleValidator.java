package ru.dreadblade.czarbank.api.validation.validator;

import lombok.RequiredArgsConstructor;
import ru.dreadblade.czarbank.api.validation.constraint.NonExistentRole;
import ru.dreadblade.czarbank.repository.security.RoleRepository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class NonExistentRoleValidator implements ConstraintValidator<NonExistentRole, String> {
    private final RoleRepository roleRepository;

    @Override
    public void initialize(NonExistentRole constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String roleName, ConstraintValidatorContext context) {
        if (roleName == null) {
            return true;
        }

        return !roleRepository.existsByName(roleName);
    }
}
