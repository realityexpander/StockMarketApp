package com.realityexpander.stockmarketapp.domain.repository

import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import com.realityexpander.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String,
    ): Flow<Resource<List<CompanyListing>>>

//    fun getStock(symbol: String): CompanyListing
//    fun addStock(stock: Stock)
//    fun removeStock(symbol: String)
}