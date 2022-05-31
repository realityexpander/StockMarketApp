package com.realityexpander.stockmarketapp.data.csv

import com.opencsv.CSVReader
import com.realityexpander.stockmarketapp.data.mapper.toIntradayInfo
import com.realityexpander.stockmarketapp.data.remote.dto.IntradayInfoDTO
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton  //                        v-- this indicates this class is available to be injected via Hilt
class IntradayInfoCSVParserImpl @Inject constructor() : CSVParser<IntradayInfo> {

    override suspend fun parse(csvStream: InputStream): List<IntradayInfo> {
        val csvReader = CSVReader(InputStreamReader(csvStream))

        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1) // drop header row
                .mapNotNull { line ->
                    val (
                        timestamp,
                        open,
                        high,
                        low,
                        close,
                        volume
                    ) = line

                    IntradayInfoDTO(
                        timestamp = timestamp ?: return@mapNotNull null,
                        open = open.toDoubleOrNull() ?: return@mapNotNull null,
                        high = high.toDoubleOrNull() ?: return@mapNotNull null,
                        low = low.toDoubleOrNull() ?: return@mapNotNull null,
                        close = close.toDoubleOrNull() ?: return@mapNotNull null,
                        volume = volume.toIntOrNull() ?: return@mapNotNull null,
                    ).toIntradayInfo()
                }
                .filter {
                    // filter only for yesterday's data
                    it.datetime.dayOfMonth == LocalDateTime.now().minusDays(1).dayOfMonth &&
                    it.datetime.monthValue == LocalDateTime.now().monthValue &&
                    it.datetime.year == LocalDateTime.now().year
                }
                .sortedBy {
                    it.datetime.hour
                }
                .also { csvReader.close() }
        }
    }
}

private operator fun <T> Array<T>.component6(): T {
    return this[5]
}
