package com.realityexpander.stockmarketapp.domain.model

import java.time.LocalDateTime

data class IntradayInfo(
    val datetime: LocalDateTime,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Int
)
