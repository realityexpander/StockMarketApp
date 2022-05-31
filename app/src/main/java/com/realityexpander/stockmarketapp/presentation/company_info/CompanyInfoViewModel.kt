package com.realityexpander.stockmarketapp.presentation.company_info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.stockmarketapp.domain.repository.StockRepository
import com.realityexpander.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StockRepository
) : ViewModel() {

    var state by mutableStateOf(CompanyInfoState())

    init {
//        savedStateHandle.get<CompanyInfoState>(key = "SAVED_STATE")?.let {
//            state = it
//        }

        viewModelScope.launch {
            val symbol = savedStateHandle.get<String>("symbol") ?: return@launch
            state = state.copy(isLoading = true)

            // make calls in parallel
            val companyInfoResult = async { repository.getCompanyInfo(symbol) }
            val intradayInfoResult = async { repository.getIntradayInfo(symbol) }

            state = when (val result = companyInfoResult.await()) {
                is Resource.Success -> {
                    state.copy(
                        isLoading = false,
                        companyInfo = result.data
                    )
                }
                is Resource.Error -> {
                    state.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Resource.Loading -> state // do nothing
            }

            state = when (val result = intradayInfoResult.await()) {
                is Resource.Success -> {
                    if (result.data == null) {
                        state.copy(
                            isLoading = false,
                            errorMessage = "No data available"
                        )
                    } else {
                        state.copy(
                            isLoading = false,
                            stockIntradayInfos = result.data
                        )
                    }

                }
                is Resource.Error -> {
                    state.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Resource.Loading -> state // do nothing
            }
        }
    }

}