//import com.xavim.testsimpleact.domain.repository.DataEntryRepository
//import kotlinx.coroutines.flow.Flow
//import javax.inject.Inject
//
//class SaveDataValueUseCase @Inject constructor(
//    private val repository: DataEntryRepository
//) {
//    operator fun invoke(
//        datasetId: String,
//        periodId: String,
//        orgUnitId: String,
//        attributeOptionComboId: String,
//        dataElementId: String,
//        categoryOptionComboId: String,
//        value: String
//    ): Flow<Result<Unit>> = repository.saveDataValue(
//        datasetId,
//        periodId,
//        orgUnitId,
//        attributeOptionComboId,
//        dataElementId,
//        categoryOptionComboId,
//        value
//    )
//}