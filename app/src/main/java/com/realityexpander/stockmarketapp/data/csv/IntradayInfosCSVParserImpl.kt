package com.realityexpander.stockmarketapp.data.csv

import com.opencsv.CSVReader
import com.realityexpander.stockmarketapp.data.mapper.DateFormatterPattern
import com.realityexpander.stockmarketapp.data.mapper.toIntradayInfo
import com.realityexpander.stockmarketapp.data.remote.dto.IntradayInfoDTO
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton  //                      v-- this indicates this class is available to be injected via Hilt
class IntradayInfosCSVParserImpl @Inject constructor() : CSVParser<IntradayInfo> {

    override suspend fun parse(csvStream: InputStream): List<IntradayInfo> {
        val csvReader = CSVReader(InputStreamReader(csvStream))

        var firstDay: LocalDateTime? = null

        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .also { array ->
                    // Hit limit for free API?
                    if(array.size == 3 && array[1][0].contains("Note")) {
                        csvReader.close()
                        throw IOException("Hit free API limit\n\n${array[1][0]}")
                    }
                }
                .drop(1) // drop header row
                .also {
                    if (it.isEmpty()) {
                        csvReader.close()
                        return@withContext emptyList()
                    }
                }
                .mapNotNull { line ->
                    val (
                        timestamp,
                        open,
                        high,
                        low,
                        close,
                        volume
                    ) = line

                    // Used to filter the first day of the time series data
                    if(firstDay == null) {
                        firstDay = LocalDateTime.parse(timestamp,
                            DateTimeFormatter.ofPattern(DateFormatterPattern))
                    }

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
                    // filter for only the first day of the time series data
                    it.datetime.dayOfMonth == firstDay?.dayOfMonth

                    // filter only for yesterday's data
                    //it.datetime.dayOfMonth == LocalDateTime.now().minusDays(1).dayOfMonth &&
                    //  it.datetime.monthValue == LocalDateTime.now().monthValue &&
                    //  it.datetime.year == LocalDateTime.now().year
                }
//                .also {
//                    it.map{
//                        println("$it, ")
//                    }
//                }
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
