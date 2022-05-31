package com.realityexpander.stockmarketapp.data.remote.dto

import com.squareup.moshi.Json

data class CompanyInfoDTO(
    @field:Json(name = "Symbol") val symbol: String?,
    @field:Json(name = "Name") val companyName: String?,
    @field:Json(name = "Exchange") val exchange: String?,
    @field:Json(name = "Description") val description: String?,
    @field:Json(name = "Industry") val industry: String?,
    @field:Json(name = "Country") val country: String?,
//    val ticker: String,
//    val website: String,
//    val sector: String,
//    val image: String,
//    val marketCapUsd: Int,
//    val logoLarge: String,
//    val logoSmall: String,
//    val overview: String,
//    val shortDescription: String,
//    val url: String,
//    val websiteUrl: String
)
