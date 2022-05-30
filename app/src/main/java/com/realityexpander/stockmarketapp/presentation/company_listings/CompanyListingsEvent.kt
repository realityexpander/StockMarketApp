package com.realityexpander.stockmarketapp.presentation.company_listings

sealed class CompanyListingsEvent{
    object OnRefresh : CompanyListingsEvent()
    data class OnSearchQueryChanged(val query: String) : CompanyListingsEvent()

}
