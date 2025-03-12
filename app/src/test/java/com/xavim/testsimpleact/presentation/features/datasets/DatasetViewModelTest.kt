package com.xavim.testsimpleact.presentation.features.datasets

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.xavim.testsimpleact.data.session.SessionManager
import com.xavim.testsimpleact.domain.model.Dataset
import com.xavim.testsimpleact.domain.repository.AuthRepository
import com.xavim.testsimpleact.domain.useCase.GetDataSetsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import dagger.hilt.android.qualifiers.ApplicationContext


@ExperimentalCoroutinesApi
class DatasetViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var sessionManager: SessionManager

    private lateinit var authRepository: AuthRepository

    private lateinit var getDataSetsUseCase: GetDataSetsUseCase

    private lateinit var viewModel: DatasetGridViewModel

    private val testDispatcher = StandardTestDispatcher()

    @ApplicationContext private val context: Context = TODO()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        // Setup
        `when`(getDataSetsUseCase.invoke()).thenReturn(flowOf(emptyList()))

        // Create view model
        viewModel = DatasetGridViewModel(sessionManager, authRepository, getDataSetsUseCase, context)

        // Initial state should be loading
        assertEquals(DatasetScreenState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `load datasets populates state with Success when datasets found`() = runTest {
        // Create test datasets
        val datasets = listOf(
            Dataset("1", "Dataset 1", "Daily", "default"),
            Dataset("2", "Dataset 2", "Monthly", "default")
        )

        // Setup
        `when`(getDataSetsUseCase.invoke()).thenReturn(flowOf(datasets))

        // Create view model
        viewModel = DatasetGridViewModel(sessionManager, authRepository,getDataSetsUseCase, context)

        // Advance time to process
        testScheduler.advanceUntilIdle()

        // Verify state
        assertTrue(viewModel.uiState.value is DatasetScreenState.Success)
        assertEquals(datasets, (viewModel.uiState.value as DatasetScreenState.Success).datasets)
    }


}