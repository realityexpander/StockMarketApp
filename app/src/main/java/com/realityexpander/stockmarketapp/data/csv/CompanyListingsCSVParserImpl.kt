package com.realityexpander.stockmarketapp.data.csv

import com.opencsv.CSVReader
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton  //                        v-- this indicates this class is available to be injected via Hilt
class CompanyListingsCSVParserImpl @Inject constructor(): CSVParser<CompanyListing> {

    override suspend fun parse(csvStream: InputStream): List<CompanyListing> {
        val csvReader = CSVReader(InputStreamReader(csvStream))

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
                .mapNotNull { line ->
                    val (symbol, name, exchange) = line

                    CompanyListing(
                        companyName = name ?: return@mapNotNull null,
                        companySymbol = symbol ?: return@mapNotNull null,
                        companyExchange = exchange ?: return@mapNotNull null
                    )
                }
                .also { csvReader.close() }
        }
    }
}