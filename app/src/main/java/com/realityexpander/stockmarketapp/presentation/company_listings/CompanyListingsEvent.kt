package com.realityexpander.stockmarketapp.presentation.company_listings

sealed class CompanyListingsEvent{
    object Refresh : CompanyListingsEvent()
    data class onSearchQueryChanged(val query: String) : CompanyListingsEvent()

}
