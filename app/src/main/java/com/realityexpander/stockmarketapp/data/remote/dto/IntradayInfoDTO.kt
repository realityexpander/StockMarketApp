package com.realityexpander.stockmarketapp.data.remote.dto

data class IntradayInfoDTO(
    val timestamp: String,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Int
)
