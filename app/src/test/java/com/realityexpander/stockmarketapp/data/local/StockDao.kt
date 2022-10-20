package com.realityexpander.stockmarketapp.data.local

import com.realityexpander.stockmarketapp.data.mapper.toCompanyListing
import com.realityexpander.stockmarketapp.data.mapper.toCompanyListingEntity
import com.realityexpander.stockmarketapp.domain.model.CompanyListing

class StockDaoFake: StockDao {

    private var companyListings = listOf<CompanyListing>()

    override suspend fun insertCompanyListings(companyListingEntities: List<CompanyListingEntity>) {
        companyListings = companyListings + companyListingEntities.map {
            it.toCompanyListing()
        }
    }

    override suspend fun clearCompanyListings() {
        companyListings = listOf()
    }

    override suspend fun searchCompanyListing(searchString: String): List<CompanyListingEntity> {
        return companyListings.filter {
            it.companyName.contains(searchString, true) ||
            it.companySymbol.contains(searchString, true) ||
            it.companyExchange.contains(searchString, true)
        }.map {
            it.toCompanyListingEntity()
        }
    }
}