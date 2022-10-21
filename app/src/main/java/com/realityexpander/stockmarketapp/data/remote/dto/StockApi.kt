package com.realityexpander.stockmarketapp.data.remote.dto

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {

    // returns a csv of company listings (only csv is supported)
    @GET("query?function=LISTING_STATUS")
    suspend fun getListOfStocksRawCSV(
        @Query("apikey") apiKey: String = API_KEY,
    ): ResponseBody

    // returns a csv of intraday stock prices
    @GET("query?function=TIME_SERIES_INTRADAY&interval=60min&datatype=csv")
    suspend fun getIntradayInfoRawCSV(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = API_KEY,
    ): ResponseBody

    // returns a json of company info
    @GET("query?function=OVERVIEW&datatype=json")
    suspend fun getCompanyInfo(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = API_KEY,
    ): CompanyInfoDTO


    companion object {
        const val BASE_URL = "https://www.alphavantage.co"
        const val API_KEY = "6EZQZAAGWP4A0JJH"
    }
}