package com.realityexpander.stockmarketapp.presentation.company_info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.stockmarketapp.data.mapper.DateFormatterPattern
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import com.realityexpander.stockmarketapp.domain.repository.StockRepository
import com.realityexpander.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CompanyInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StockRepository
) : ViewModel() {

    var state by mutableStateOf(CompanyInfoState())

    init {

        viewModelScope.launch {
            val symbol = savedStateHandle.get<String>("symbol") ?: return@launch
            state = state.copy(isLoadingIntradayInfos = true, isLoadingCompanyInfo = true)

            // make calls in parallel
            val companyInfoResult = async {
                repository.getCompanyInfo(symbol)
            }
            val intradayInfosResult = async {
                repository.getIntradayInfos(symbol)
                // intradayInfoResultSample2() // Sample data
            }


            state = when (val result = companyInfoResult.await()) {
                is Resource.Success -> {
                        state.copy(
                            isLoadingCompanyInfo = false,
                            companyInfo = result.data
                        )
                }
                is Resource.Error -> {
                    state.copy(
                        isLoadingCompanyInfo = false,
                        errorMessageCompanyInfo = result.message,
                        companyInfo = null
                    )
                }
                is Resource.Loading -> state // do nothing
            }

            state = when (val result = intradayInfosResult.await()) {
                is Resource.Success -> {
                    if (result.data == null) {
                        state.copy(
                            isLoadingIntradayInfos = false,
                            errorMessageIntradayInfos = "Data not available.",
                            intradayInfos = emptyList()
                        )
                    } else {
                        state.copy(
                            isLoadingIntradayInfos = false,
                            intradayInfos = result.data,
                            errorMessageIntradayInfos = null
                        )
                    }

                }
                is Resource.Error -> {
                    state.copy(
                        isLoadingIntradayInfos = false,
                        errorMessageIntradayInfos = result.message,
                        intradayInfos = emptyList()
                    )
                }
                is Resource.Loading -> state // do nothing
            }
        }
    }

}



// Sample data

fun intradayInfoResultSample(): Resource<List<IntradayInfo>> {
    val fmt = DateTimeFormatter.ofPattern(DateFormatterPattern)
    return Resource.Success(
        listOf<IntradayInfo>(
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 01:00:00", fmt),
                close = 50.0,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 02:00:00", fmt),
                close = 6.5,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 03:00:00", fmt),
                close = 70.0,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 04:00:00", fmt),
                close = 152.38,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 05:00:00", fmt),
                close = 120.0,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 06:00:00", fmt),
                close = 50.0,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 07:00:00", fmt),
                close = 60.0,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 08:00:00", fmt),
                close = 148.89,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 09:00:00", fmt),
                close = 80.0,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 10:00:00", fmt),
                close = 20.0,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 11:00:00", fmt),
                close = 8.8,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2020-05-31 13:00:00", fmt),
                close = 90.0,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
        )
    )
}

fun intradayInfoResultSample2(): Resource<List<IntradayInfo>> {
    val fmt = DateTimeFormatter.ofPattern(DateFormatterPattern)
    return Resource.Success(
        listOf<IntradayInfo>(
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 20:00:00", fmt),
                close = 61.9500,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 19:00:00", fmt),
                close = 61.7000,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 17:00:00", fmt),
                close = 61.5000,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 16:00:00", fmt),
                close = 61.7100,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 15:00:00", fmt),
                close = 61.5500,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 14:00:00", fmt),
                close = 63.1700,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 13:00:00", fmt),
                close = 63.2500,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 12:00:00", fmt),
                close = 62.7150,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 11:00:00", fmt),
                close = 63.4100,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 10:00:00", fmt),
                close = 64.3200,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 09:00:00", fmt),
                close = 65.7000,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 07:00:00", fmt),
                close = 64.5000,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 08:00:00", fmt),
                close = 61.7100,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 06:00:00", fmt),
                close = 64.9600,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 05:00:00", fmt),
                close = 61.0000,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
            IntradayInfo(
                LocalDateTime.parse("2022-05-31 04:00:00", fmt),
                close = 60.8200,
                high = 0.0,
                low = 0.0,
                open = 0.0,
                volume = 100
            ),
        ).reversed()
    )
}