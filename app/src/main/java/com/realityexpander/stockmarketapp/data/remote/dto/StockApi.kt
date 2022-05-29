package com.realityexpander.stockmarketapp.data.remote.dto

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {

    @GET("query?function=LISTING_STATUS")
    suspend fun getListOfStocks(
        @Query("apikey") apiKey: String,
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://www.alphavantage.co"
        const val API_KEY = "6EZQZAAGWP4A0JJH"
    }
}