class ValidateDataEntryUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    operator fun invoke(values: Map<String, String>): List<ValidationError> =
        repository.validateAllValues(values)

    fun validateField(elementId: String, value: String): List<ValidationError> =
        repository.validateDataValue(elementId, value)
}