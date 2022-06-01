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
            state = state.copy(isLoading = true)

            // make calls in parallel
            val companyInfoResult = async { repository.getCompanyInfo(symbol) }
//            val intradayInfoResult = async { repository.getIntradayInfo(symbol) }

            val fmt = DateTimeFormatter.ofPattern(DateFormatterPattern)
            val intradayInfoResult = async {
                val response: Resource<List<IntradayInfo>> = Resource.Success(listOf<IntradayInfo>(
                    IntradayInfo(LocalDateTime.parse("2020-05-31 01:00:00",fmt), close = 50.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 02:00:00",fmt), close = 60.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 03:00:00",fmt), close = 70.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 04:00:00",fmt), close = 30.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 05:00:00",fmt), close = 120.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 06:00:00",fmt), close = 50.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 07:00:00",fmt), close = 60.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 08:00:00",fmt), close = 70.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 09:00:00",fmt), close = 80.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 10:00:00",fmt), close = 20.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                    IntradayInfo(LocalDateTime.parse("2020-05-31 11:00:00",fmt), close = 10.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
                ))
                response
            }

            state = when (val result = companyInfoResult.await()) {
                is Resource.Success -> {
                    state.copy(
                        //isLoading = false,
                        companyInfo = result.data,
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    state.copy(
                        //isLoading = false,
                        errorMessage = result.message,
                        companyInfo = null
                    )
                }
                is Resource.Loading -> state // do nothing
            }

            state = when (val result = intradayInfoResult.await()) {
                is Resource.Success -> {
                    if (result.data == null) {
                        state.copy(
                            isLoading = false,
                            errorMessage = "Data not available.",
                            stockIntradayInfos = emptyList()
                        )
                    } else {
                        state.copy(
                            isLoading = false,
                            stockIntradayInfos = result.data,
                            errorMessage = null
                        )
                    }

                }
                is Resource.Error -> {
                    state.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        stockIntradayInfos = emptyList()
                    )
                }
                is Resource.Loading -> state // do nothing
            }
        }
    }

}