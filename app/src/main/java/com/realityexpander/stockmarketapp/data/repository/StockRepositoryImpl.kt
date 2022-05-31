package com.realityexpander.stockmarketapp.data.repository

import com.realityexpander.stockmarketapp.data.csv.CSVParser
import com.realityexpander.stockmarketapp.data.local.StockDatabase
import com.realityexpander.stockmarketapp.data.mapper.toCompanyInfo
import com.realityexpander.stockmarketapp.data.mapper.toCompanyListing
import com.realityexpander.stockmarketapp.data.mapper.toCompanyListingEntity
import com.realityexpander.stockmarketapp.data.remote.dto.StockApi
import com.realityexpander.stockmarketapp.domain.model.CompanyInfo
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import com.realityexpander.stockmarketapp.domain.repository.StockRepository
import com.realityexpander.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsCSVParser: CSVParser<CompanyListing>,
    private val intradayInfoCSVParser: CSVParser<IntradayInfo>,
): StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))

            // Attempt to load from local cache.
            val localListings = dao.searchCompanyListing(query)
            emit(Resource.Success(
                data = localListings.map { it.toCompanyListing() },
            ))

            // Check if cache is empty.
            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if(shouldJustLoadFromCache) {
                emit(Resource.Loading(false ))
                return@flow
            }

            // Attempt to load from remote.
            val remoteListings = try {
                val response = api.getListOfStocks() // returns a CSV file.
                companyListingsCSVParser.parse(response.byteStream())
            } catch (e: IOException) { // parse error
                e.printStackTrace()
                emit(Resource.Error(e.localizedMessage ?: "Error loading or parsing company listings"))
                null
            } catch (e: HttpException) { // invalid network response
                e.printStackTrace()
                emit(Resource.Error(e.localizedMessage ?: "Error with network for company listings"))
                null
            }

            // Save to local cache.
            remoteListings?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity() }
                )

                // Get listings from local cache, yes this is tiny bit inefficient but conforms to SSOT
                emit(Resource.Success(
                    data = dao
                        .searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(false))
            }
        }
    }


    override suspend fun getIntradayInfo(stockSymbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(stockSymbol).byteStream()
            val results = intradayInfoCSVParser.parse(response)
            Resource.Success(results)
        } catch (e: IOException) { // parse error
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Error loading or parsing intraday info")
        } catch (e: HttpException) { // invalid network response
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Error with network for intraday info")
        }
    }

    override suspend fun getCompanyInfo(stockSymbol: String): Resource<CompanyInfo> {
        return try {
            val response = api.getCompanyInfo(stockSymbol)
            Resource.Success(response.toCompanyInfo())
        } catch (e: IOException) { // parse error
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Error loading or parsing company info")
        } catch (e: HttpException) { // invalid network response
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Error with network for company info")
        }
    }
}



























