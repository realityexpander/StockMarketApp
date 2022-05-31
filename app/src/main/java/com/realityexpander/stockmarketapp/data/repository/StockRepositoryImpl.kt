package com.realityexpander.stockmarketapp.data.repository

import com.realityexpander.stockmarketapp.data.csv.CSVParser
import com.realityexpander.stockmarketapp.data.local.StockDatabase
import com.realityexpander.stockmarketapp.data.mapper.toCompanyListing
import com.realityexpander.stockmarketapp.data.mapper.toCompanyListingEntity
import com.realityexpander.stockmarketapp.data.remote.dto.StockApi
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
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
    private val companyListingsParser: CSVParser<CompanyListing>,
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
                val response = api.getListOfStocks()
                companyListingsParser.parse(response.byteStream())
            } catch (e: IOException) { // parse error
                e.printStackTrace()
                emit(Resource.Error(e.localizedMessage ?: "Error loading or parsing data"))
                null
            } catch (e: HttpException) { // invalid network response
                e.printStackTrace()
                emit(Resource.Error(e.localizedMessage ?: "Error with network"))
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

}