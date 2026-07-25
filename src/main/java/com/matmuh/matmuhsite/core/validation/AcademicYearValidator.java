package com.matmuh.matmuhsite.core.validation;

import com.matmuh.matmuhsite.core.helpers.AcademicYears;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AcademicYearValidator implements ConstraintValidator<AcademicYear, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || AcademicYears.isValid(value);
    }
}
