class CreateNewEntryUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    fun generatePeriodId(): String = repository.generatePeriodId()
    fun getDefaultOrgUnitId(): String = repository.getDefaultOrgUnitId()
    fun getDefaultAttributeOptionComboId(): String = repository.getDefaultAttributeOptionComboId()
}