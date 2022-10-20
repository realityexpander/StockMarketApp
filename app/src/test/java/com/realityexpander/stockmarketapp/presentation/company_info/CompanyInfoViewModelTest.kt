package com.realityexpander.stockmarketapp.presentation.company_info

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.realityexpander.stockmarketapp.MainCoroutineRule
import com.realityexpander.stockmarketapp.data.repository.StockRepositoryFake
import com.realityexpander.stockmarketapp.domain.model.CompanyInfo
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class CompanyInfoViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var viewModel: CompanyInfoViewModel
    private lateinit var repositoryFake: StockRepositoryFake

    @Before
    fun setUp() {
        repositoryFake = StockRepositoryFake()
        viewModel = CompanyInfoViewModel(
            savedStateHandle = SavedStateHandle(
                initialState = mapOf(
                    "symbol" to "GOOGL"
                )
            ),
            repository = repositoryFake
        )
    }

    @Test
    fun `Company and intra-day info are properly mapped to state`() {
        // ARRANGE
        val companyInfo = CompanyInfo(
            symbol = "GOOGL",
            description = "Google desc",
            companyName = "Google",
            country = "USA",
            industry = "Tech",
            exchange = "NASDAQ"
        )
        repositoryFake.companyInfoToReturn = companyInfo

        val intradayInfos = listOf(
            IntradayInfo(
                datetime = LocalDateTime.now(),
                close = 10.0,
                open = 10.0,
                high = 10.0,
                low = 10.0,
                volume = 100
            )
        )
        repositoryFake.intradayInfosToReturn = intradayInfos

        // ACT
        coroutineRule.dispatcher.scheduler.advanceUntilIdle()

        // ASSERT
        assertThat(viewModel.state.companyInfo).isEqualTo(companyInfo)
        assertThat(viewModel.state.intradayInfos).isEqualTo(intradayInfos)
    }
}