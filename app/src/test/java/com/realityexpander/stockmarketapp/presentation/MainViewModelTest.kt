package com.realityexpander.stockmarketapp.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.realityexpander.stockmarketapp.data.RepositoryModel
import com.realityexpander.stockmarketapp.domain.GetRepositoriesUseCase
import com.realityexpander.stockmarketapp.utils.LiveDataResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MainViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var getRepositoriesUseCase: GetRepositoriesUseCase

    lateinit var mainViewModel: MainViewModel
    private val dispatcher = Dispatchers.Unconfined

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mainViewModel = MainViewModel(dispatcher, dispatcher, getRepositoriesUseCase)
    }

    @Test
    fun testFetchRepositoriesPositive() {
        runBlocking {
            coEvery { getRepositoriesUseCase.execute(any()) } returns listOf(
                RepositoryModel("1", "Mockito"),
                RepositoryModel("2", "TDD"),
                RepositoryModel("3", "RXjava")
            )

            mainViewModel.fetchRepositories("irtizakh")

            assert(mainViewModel.repositoriesLiveData.value != null)
            assert(mainViewModel.repositoriesLiveData.value!!.status == LiveDataResult.STATUS.SUCCESS)
        }
    }

    @Test
    fun testFetchRepositoriesNegative() {
        coEvery { getRepositoriesUseCase.execute(any()) } coAnswers {
            throw Exception("No network")
        }

        mainViewModel.fetchRepositories("irtizakh")

        assert(mainViewModel.repositoriesLiveData.value != null)
        assert(mainViewModel.repositoriesLiveData.value!!.status == LiveDataResult.STATUS.ERROR)
    }

}