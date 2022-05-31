package com.realityexpander.stockmarketapp.data.mapper

import com.realityexpander.stockmarketapp.data.remote.dto.IntradayInfoDTO
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val DateFormatterPattern = "yyyy-MM-dd HH:mm:ss"

fun IntradayInfo.toIntradayInfoDTO() = IntradayInfoDTO(
    datetime.format(DateTimeFormatter.ofPattern(DateFormatterPattern)),
    open,
    high,
    low,
    close,
    volume,
)

fun IntradayInfoDTO.toIntradayInfo() = IntradayInfo(
    datetime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern(DateFormatterPattern)),
    open = open,
    high = high,
    low = low,
    close = close,
    volume = volume,
)