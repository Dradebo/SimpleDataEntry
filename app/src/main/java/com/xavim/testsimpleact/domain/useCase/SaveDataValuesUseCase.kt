class SaveDataValueUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    operator fun invoke(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        dataElementId: String,
        categoryOptionComboId: String,
        value: String
    ): Flow<Result<Unit>> = repository.saveDataValue(
        datasetId,
        periodId,
        orgUnitId,
        attributeOptionComboId,
        dataElementId,
        categoryOptionComboId,
        value
    )
}