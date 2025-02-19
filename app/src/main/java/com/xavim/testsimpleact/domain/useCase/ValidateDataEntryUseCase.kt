package com.xavim.testsimpleact.domain.useCase

class ValidateDataEntryUseCase @Inject constructor() {
    operator fun invoke(values: Map<String, String>): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        values.forEach { (elementId, value) ->
            when {
                value.isEmpty() -> {
                    errors.add(ValidationError(elementId, "Field cannot be empty"))
                }
                // Add more validation rules as needed
            }
        }

        return errors
    }
}

data class ValidationError(
    val elementId: String,
    val message: String
)