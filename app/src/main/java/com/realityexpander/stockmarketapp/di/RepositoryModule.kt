package com.realityexpander.stockmarketapp.di

import com.realityexpander.stockmarketapp.data.csv.CSVParser
import com.realityexpander.stockmarketapp.data.csv.CompanyListingsCSVParserImpl
import com.realityexpander.stockmarketapp.data.csv.IntradayInfoCSVParserImpl
import com.realityexpander.stockmarketapp.data.repository.StockRepositoryImpl
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import com.realityexpander.stockmarketapp.domain.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCompanyListingsCSVParser(
        companyListingsCSVParserImpl: CompanyListingsCSVParserImpl // <-- provides this instance...
    ): CSVParser<CompanyListing> // <-- ... of this interface.

    @Binds
    @Singleton
    abstract fun bindIntradayInfoCSVParser(
        intradayInfoCSVParserImpl: IntradayInfoCSVParserImpl // <-- provides this instance...
    ): CSVParser<IntradayInfo> // <-- ... of this interface.

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl
    ): StockRepository
}