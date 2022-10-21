package com.realityexpander.stockmarketapp.data.repository

import com.realityexpander.stockmarketapp.domain.model.CompanyInfo
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import com.realityexpander.stockmarketapp.domain.repository.IStockRepository
import com.realityexpander.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime

class StockRepositoryFake: IStockRepository {

    // Simulate a database with 10 companies
    var companyListingsToReturn = (1..10).map {
        CompanyListing(
            companyName = "name$it",
            companySymbol = "symbol$it",
            companyExchange = "exchange$it"
        )
    }
    var intradayInfosToReturn = (1..10).map {
        IntradayInfo(
            datetime = LocalDateTime.now(),
            close = it.toDouble(),
            open = it.toDouble(),
            high = it.toDouble(),
            low = it.toDouble(),
            volume = it * 100
        )
    }
    var companyInfoToReturn = CompanyInfo(
        symbol = "symbol",
        description = "description",
        companyName = "name",
        country = "country",
        industry = "industry",
        exchange = "exchange",
    )

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Success(companyListingsToReturn))
        }
    }

    override suspend fun getIntradayInfos(stockSymbol: String): Resource<List<IntradayInfo>> {
        return Resource.Success(intradayInfosToReturn)
    }

    override suspend fun getIntradayInfosWithoutCatches(stockSymbol: String): Resource<List<IntradayInfo>> {
        return Resource.Success(intradayInfosToReturn)
    }

    override suspend fun getCompanyInfo(stockSymbol: String): Resource<CompanyInfo> {
        return Resource.Success(companyInfoToReturn)
    }
}