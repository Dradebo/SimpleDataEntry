package com.xavim.testsimpleact.di

//import CreateNewEntryUseCase
//import GetDataEntryFormUseCase
//import GetExistingDataValuesUseCase
//import SaveDataEntryUseCase
import android.content.Context
import com.xavim.testsimpleact.data.repositoryImpl.AndroidLogger
import com.xavim.testsimpleact.data.repositoryImpl.AuthRepositoryImpl
//import com.xavim.testsimpleact.data.repositoryImpl.DataEntryRepositoryImpl
//import com.xavim.testsimpleact.data.repositoryImpl.DatasetInstanceRepositoryImpl
import com.xavim.testsimpleact.data.repositoryImpl.DatasetRepositoryImpl
import com.xavim.testsimpleact.data.repositoryImpl.SystemRepositoryImpl
import com.xavim.testsimpleact.data.session.PreferenceProvider
import com.xavim.testsimpleact.data.session.SessionManager
import com.xavim.testsimpleact.domain.repository.AuthRepository
//import com.xavim.testsimpleact.domain.repository.DataEntryRepository
//import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import com.xavim.testsimpleact.domain.repository.DatasetRepository
import com.xavim.testsimpleact.domain.repository.Logger
import com.xavim.testsimpleact.domain.repository.SystemRepository
import com.xavim.testsimpleact.domain.useCase.GetDataSetsUseCase
//import com.xavim.testsimpleact.domain.useCase.GetDatasetInstancesUseCase
//import com.xavim.testsimpleact.domain.useCase.ValidateDataEntryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    fun providePreferenceProvider(@ApplicationContext context: Context): PreferenceProvider {
        return PreferenceProvider(context)
    }

    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return AndroidLogger()
    }

    @Provides
    @Singleton
    fun provideD2Provider(sessionManager: SessionManager): () -> D2? {
        return {
            try {
                sessionManager.getD2()
            } catch (e: Exception) {
                null
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context, d2Provider: () -> D2?): AuthRepository {
        return AuthRepositoryImpl(context, d2Provider)
    }

    @Provides
    @Singleton
    fun provideSystemRepository(sessionManager: SessionManager, logger: AndroidLogger): SystemRepository {
        return SystemRepositoryImpl(sessionManager, logger)
    }

    @Provides
    @Singleton
    fun provideDatasetRepository(sessionManager: SessionManager): DatasetRepository {
        val d2 = sessionManager.getD2()
        return DatasetRepositoryImpl(d2)
    }

    @Provides
    fun provideGetDataSetsUseCase(repository: DatasetRepository): GetDataSetsUseCase {
        return GetDataSetsUseCase(repository)
    }

//    @Provides
//    @Singleton
//    fun provideDatasetInstanceRepository(sessionManager: SessionManager): DatasetInstanceRepository {
//        val d2 = sessionManager.getD2()
//        return DatasetInstanceRepositoryImpl(d2)
//    }

//    @Provides
//    fun provideGetDatasetInstancesUseCase(datasetInstance: DatasetInstanceRepository): GetDatasetInstancesUseCase {
//        return GetDatasetInstancesUseCase(datasetInstance)
//    }
//
//    @Provides
//    @Singleton
//    fun provideEntryRepository(sessionManager: SessionManager): DataEntryRepository {
//        val d2 = sessionManager.getD2()
//        return DataEntryRepositoryImpl(d2)
//    }
//
//    @Provides
//    fun provideGetDataValuesUseCase(dataEntry: DataEntryRepository): GetDataEntryFormUseCase {
//        return GetDataEntryFormUseCase(dataEntry)
//    }
//
//    @Provides
//    @Singleton
//    fun provideCreateNewEntryUseCase(dataEntry: DataEntryRepository): CreateNewEntryUseCase {
//        return CreateNewEntryUseCase(dataEntry)
//    }
//
//    @Provides
//    fun provideGetDataEntryFormUseCase(repository: DataEntryRepository): GetDataEntryFormUseCase =
//        GetDataEntryFormUseCase(repository)
//
//    @Provides
//    fun provideGetExistingDataValuesUseCase(repository: DataEntryRepository): GetExistingDataValuesUseCase =
//        GetExistingDataValuesUseCase(repository)
//
//    @Provides
//    fun provideSaveDataEntryUseCase(repository: DataEntryRepository): SaveDataEntryUseCase =
//        SaveDataEntryUseCase(repository)
//
//    @Provides
//    fun provideValidateDataEntryUseCase(repository: DataEntryRepository): ValidateDataEntryUseCase =
//        ValidateDataEntryUseCase(repository)

    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}