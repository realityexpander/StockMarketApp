package com.realityexpander.stockmarketapp.domain.repository

import com.realityexpander.stockmarketapp.domain.model.CompanyInfo
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import com.realityexpander.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface IStockRepository {
    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String,
    ): Flow<Resource<List<CompanyListing>>>

    suspend fun getCompanyInfo(
        stockSymbol: String,
    ): Resource<CompanyInfo>

    suspend fun getIntradayInfos(
        stockSymbol: String,
    ): Resource<List<IntradayInfo>>

//     To be implemented in the future
//    fun getStock(symbol: String): CompanyListing
//    fun addStock(stock: Stock)
//    fun removeStock(symbol: String)
}