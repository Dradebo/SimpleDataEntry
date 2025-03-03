class GetDataEntryFormUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    operator fun invoke(dataSetUid: String): Flow<List<DataEntrySection>> =
        repository.getDataEntryForm(dataSetUid)
}