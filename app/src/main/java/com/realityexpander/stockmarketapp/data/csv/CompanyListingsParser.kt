package com.realityexpander.stockmarketapp.data.csv

import com.opencsv.CSVReader
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyListingsParser @Inject constructor(): CSVParser<CompanyListing> {
    override suspend fun parse(csvStream: InputStream): List<CompanyListing> {
        val csvReader = CSVReader(InputStreamReader(csvStream))

        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1) // drop header
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