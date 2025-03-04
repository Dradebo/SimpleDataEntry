package com.xavim.testsimpleact.di

import CreateNewEntryUseCase
import GetDataEntryFormUseCase
import GetExistingDataValuesUseCase
import SaveDataEntryUseCase
import com.xavim.testsimpleact.domain.useCase.ValidateDataEntryUseCase
import android.content.Context
import com.xavim.testsimpleact.data.repositoryImpl.AndroidLogger
import com.xavim.testsimpleact.data.repositoryImpl.AuthRepositoryImpl
import com.xavim.testsimpleact.data.repositoryImpl.DataEntryRepositoryImpl
import com.xavim.testsimpleact.data.repositoryImpl.DatasetInstanceRepositoryImpl
import com.xavim.testsimpleact.data.repositoryImpl.DatasetRepositoryImpl
import com.xavim.testsimpleact.data.repositoryImpl.SystemRepositoryImpl
import com.xavim.testsimpleact.data.session.PreferenceProvider
import com.xavim.testsimpleact.data.session.SessionManager
import com.xavim.testsimpleact.domain.repository.AuthRepository
import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import com.xavim.testsimpleact.domain.repository.DatasetRepository
import com.xavim.testsimpleact.domain.repository.Logger
import com.xavim.testsimpleact.domain.repository.SystemRepository
import com.xavim.testsimpleact.domain.useCase.CompleteDataEntryUseCase
import com.xavim.testsimpleact.domain.useCase.GetDataSetsUseCase
import com.xavim.testsimpleact.domain.useCase.GetDatasetInstancesUseCase

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hisp.dhis.android.core.D2
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(logger: AndroidLogger, preferenceProvider: PreferenceProvider): SessionManager {
        return SessionManager(logger, preferenceProvider)
    }




    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return AndroidLogger()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(context: Context, d2Provider: () -> D2?): AuthRepository {
        return AuthRepositoryImpl(context, d2Provider)
    }


    @Provides
    @Singleton
    fun provideSystemRepository(sessionManager: SessionManager, logger: AndroidLogger): SystemRepository {

        return SystemRepositoryImpl(sessionManager, logger)
    }

    /**
     * DatasetRepository for retrieving data from DHIS2. Uses the D2 instance from SessionManager.
     * This ensures that D2 is properly initialized in the background before usage.
     */
    @Provides
    @Singleton
    fun provideDatasetRepository(
        sessionManager: SessionManager
    ): DatasetRepository {
        val d2 = sessionManager.getD2()
        // If d2 is null at this point, we might throw an exception
        // or handle a fallback scenario. For brevity, we assume d2 is non-null after init.
        return d2?.let { DatasetRepositoryImpl(it) }
            ?: throw IllegalStateException("D2 is not initialized. Check session initialization.")
    }

    /**
     * UseCase for retrieving all datasets.
     */
    @Provides
    fun provideGetDataSetsUseCase(
        repository: DatasetRepository
    ): GetDataSetsUseCase {
        return GetDataSetsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDatasetInstanceRepository(
        sessionManager: SessionManager
    ): DatasetInstanceRepository {
        val d2 = sessionManager.getD2()
        // If d2 is null at this point, we might throw an exception
        // or handle a fallback scenario. For brevity, we assume d2 is non-null after init.
        return d2.let { DatasetInstanceRepositoryImpl(it) }
            ?: throw IllegalStateException("D2 is not initialized. Check session initialization.")
    }

    /**
     * UseCase for retrieving all datasets.
     */
    @Provides
    fun provideGetDatasetInstancesUseCase(
        datasetInstance: DatasetInstanceRepository
    ): GetDatasetInstancesUseCase {
        return GetDatasetInstancesUseCase(datasetInstance)
    }

    @Provides
    @Singleton
    fun provideEntryRepository(
        sessionManager: SessionManager
    ): DataEntryRepository {
        val d2 = sessionManager.getD2()
        // If d2 is null at this point, we might throw an exception
        // or handle a fallback scenario. For brevity, we assume d2 is non-null after init.
        return d2?.let { DataEntryRepositoryImpl(it) }
            ?: throw IllegalStateException("D2 is not initialized. Check session initialization.")
    }

    /**
     * UseCase for retrieving all datasets.
     */
    @Provides
    fun provideGetDataValuesUseCase(
        dataEntry: DataEntryRepository
    ): GetDataEntryFormUseCase {
        return GetDataEntryFormUseCase(dataEntry)
    }

    @Provides
    @Singleton // If you want a single instance
    fun provideCreateNewEntryUseCase(
       dataEntry: DataEntryRepository
    ): CreateNewEntryUseCase {
        return CreateNewEntryUseCase(dataEntry)
    }

//    @Provides
//    fun provideGetDatasetInstancesUseCase(
//        repository: DatasetInstanceRepository
//    ): GetDatasetInstancesUseCase =
//        GetDatasetInstancesUseCase(repository)

    @Provides
    fun provideGetDataEntryFormUseCase(
        repository: DataEntryRepository
    ): GetDataEntryFormUseCase =
        GetDataEntryFormUseCase(repository)

    @Provides
    fun provideGetExistingDataValuesUseCase(
        repository: DataEntryRepository
    ): GetExistingDataValuesUseCase =
        GetExistingDataValuesUseCase(repository)

    @Provides
    fun provideSaveDataEntryUseCase(
        repository: DataEntryRepository
    ): SaveDataEntryUseCase =
        SaveDataEntryUseCase(repository)

    @Provides
    fun provideValidateDataEntryUseCase(): ValidateDataEntryUseCase =
        ValidateDataEntryUseCase()

    @Provides
    fun provideCompleteDataEntryUseCase(
        repository: DataEntryRepository
    ): CompleteDataEntryUseCase =
        CompleteDataEntryUseCase(repository)
}