package com.realityexpander.stockmarketapp.data.mapper

import com.realityexpander.stockmarketapp.data.local.CompanyListingEntity
import com.realityexpander.stockmarketapp.data.remote.dto.CompanyInfoDTO
import com.realityexpander.stockmarketapp.domain.model.CompanyInfo
import com.realityexpander.stockmarketapp.domain.model.CompanyListing

///// CompanyListing mappers

fun CompanyListing.toCompanyListingEntity(): CompanyListingEntity {
    return CompanyListingEntity(
        symbol = companySymbol,
        name = companyName,
        exchange = companyExchange,
    )
}

fun CompanyListingEntity.toCompanyListing(): CompanyListing {
    return CompanyListing(
        companySymbol = symbol,
        companyName = name,
        companyExchange = exchange,
    )
}


//// CompanyInfo mappers

fun CompanyInfo.toCompanyInfoDTO() = CompanyInfoDTO(
    symbol = symbol,
    companyName = companyName,
    exchange = exchange,
    description = description,
    industry = industry,
    country = country,
)

fun CompanyInfoDTO.toCompanyInfo() = CompanyInfo(
    symbol = symbol ?: "",
    companyName = companyName ?: "",
    exchange = exchange ?: "",
    description = description ?: "",
    industry = industry ?: "",
    country = country ?: "",
)
