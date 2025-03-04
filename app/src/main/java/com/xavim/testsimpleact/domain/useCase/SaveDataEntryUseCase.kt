import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveDataEntryUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    operator fun invoke(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        values: Map<String, String>,
        isNewEntry: Boolean
    ): Flow<Result<Unit>> = repository.saveDataValues(
        datasetId,
        periodId,
        orgUnitId,
        attributeOptionComboId,
        values
    )
}