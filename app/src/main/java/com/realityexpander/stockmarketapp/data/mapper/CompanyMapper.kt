package com.realityexpander.stockmarketapp.data.mapper

import com.realityexpander.stockmarketapp.data.local.CompanyListingEntity
import com.realityexpander.stockmarketapp.domain.model.CompanyListing

fun CompanyListingEntity.toCompanyListing(): CompanyListing {
    return CompanyListing(
        companySymbol = symbol,
        companyName = name,
        companyExchange = exchange,
    )
}

fun CompanyListing.toCompanyListingEntity(): CompanyListingEntity {
    return CompanyListingEntity(
        symbol = companySymbol,
        name = companyName,
        exchange = companyExchange,
    )
}