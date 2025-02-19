package com.xavim.testsimpleact.domain.useCase

class SaveDataEntryUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    suspend operator fun invoke(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        values: Map<String, String>,
        isNewEntry: Boolean
    ) {
        if (isNewEntry) {
            repository.createDataEntry(
                datasetId,
                periodId,
                orgUnitId,
                attributeOptionComboId,
                values
            )
        } else {
            repository.updateDataValues(
                datasetId,
                periodId,
                orgUnitId,
                attributeOptionComboId,
                values
            )
        }
    }
}