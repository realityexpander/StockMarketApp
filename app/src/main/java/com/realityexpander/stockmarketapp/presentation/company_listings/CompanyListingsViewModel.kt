package com.realityexpander.stockmarketapp.presentation.company_listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.stockmarketapp.domain.repository.StockRepository
import com.realityexpander.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyListingsViewModel @Inject constructor(
    private val repository: StockRepository
): ViewModel() {

    var state by mutableStateOf(CompanyListingsState())

    private var searchJob: Job? = null

    fun onEvent(event: CompanyListingsEvent) {
        when (event) {
            is CompanyListingsEvent.OnRefresh -> {
                getCompanyListings(state.searchQuery, true)
            }
            is CompanyListingsEvent.OnSearchQueryChanged -> {
                state = state.copy(searchQuery = event.query)

                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500) // wait for 500ms to throttle the search
                    getCompanyListings(event.query, false)
                }
            }
        }
    }

    private fun getCompanyListings(
        query: String = state.searchQuery.lowercase(),
        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch {
            repository
                .getCompanyListings(fetchFromRemote, query)
                .collect { result ->
                    state = when(result) {
                        is Resource.Success -> state.copy(companyListings = result.data ?: emptyList())
                        is Resource.Error -> state.copy(errorMessage = result.message)
                        is Resource.Loading -> state.copy(isLoading = result.isLoading)
                    }
                }
        }
    }
}
