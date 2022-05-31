package com.realityexpander.stockmarketapp.presentation.company_listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.stockmarketapp.domain.repository.StockRepository
import com.realityexpander.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class CompanyListingsViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    var state by mutableStateOf(CompanyListingsState())

    private var searchJob: Job? = null

    init {
        state = state.copy(isLoading = true)
        runBlocking {delay(500)} // To show loading state
        getCompanyListings("", false)
    }

    fun onEvent(event: CompanyListingsEvent) {

        when (event) {
            is CompanyListingsEvent.OnRefresh -> {
                clearErrorMessage()
                getCompanyListings(state.searchQuery, true)
            }
            is CompanyListingsEvent.OnSearchQueryChanged -> {
                clearErrorMessage()
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
            withContext(Dispatchers.IO) {
                repository
                    .getCompanyListings(fetchFromRemote, query)
                    .collect { result ->
                        withContext(Dispatchers.Main) {
                            state = when (result) {
                                is Resource.Success -> state.copy(
                                    companyListings = result.data ?: emptyList()
                                )
                                is Resource.Error -> state.copy(errorMessage = result.message)
                                is Resource.Loading -> state.copy(isLoading = result.isLoading)
                            }
                        }
                    }
            }
        }
    }

    private fun clearErrorMessage() {
        state = state.copy(errorMessage = null)
    }
}
