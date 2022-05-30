package com.realityexpander.stockmarketapp.presentation.company_listings

import com.realityexpander.stockmarketapp.domain.model.CompanyListing

data class CompanyListingsState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val companyListings: List<CompanyListing> = emptyList(),
)
