package com.realityexpander.stockmarketapp.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.realityexpander.stockmarketapp.data.RepositoryModel
import com.realityexpander.stockmarketapp.data.remote.dto.StockApi
import com.realityexpander.stockmarketapp.domain.GetRepositoriesUseCase
import com.realityexpander.stockmarketapp.util.Resource
import com.realityexpander.stockmarketapp.utils.LiveDataResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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

    @MockK
    lateinit var api: StockApi

    lateinit var mainViewModel: MainViewModel
    private val dispatcher = Dispatchers.Unconfined

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mainViewModel = MainViewModel(dispatcher, dispatcher, getRepositoriesUseCase, api)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testFetchIntradayInfosNegative() {
//        coEvery { mainViewModel.getIntradayInfos(any()) } coAnswers {
        coEvery { api.getIntradayInfoRawCSV(any()) } coAnswers {
            throw Exception("No network")
        }

        runTest {
            val response = mainViewModel.getIntradayInfos("irtizakh")

            assert(response is Resource.Error)
            assert(response.message != null)
            assert(response.message == "No network")
        }
    }

}