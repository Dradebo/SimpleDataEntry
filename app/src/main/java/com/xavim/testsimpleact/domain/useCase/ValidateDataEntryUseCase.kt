package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.model.ValidationError
import javax.inject.Inject

class ValidateDataEntryUseCase @Inject constructor() {
    operator fun invoke(values: Map<String, String>): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        values.forEach { (elementId, value) ->
            errors.addAll(validateField(elementId, value))
        }

        return errors
    }

    fun validateField(elementId: String, value: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // Add your validation logic here
        // For example:
        if (value.isEmpty()) {
            errors.add(ValidationError(elementId, "Field cannot be empty"))
        }

        return errors
    }
}