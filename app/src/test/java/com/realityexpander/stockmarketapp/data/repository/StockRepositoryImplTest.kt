package com.realityexpander.stockmarketapp.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.realityexpander.stockmarketapp.data.csv.CSVParser
import com.realityexpander.stockmarketapp.data.local.CompanyListingEntity
import com.realityexpander.stockmarketapp.data.local.StockDao
import com.realityexpander.stockmarketapp.data.local.StockDaoFake
import com.realityexpander.stockmarketapp.data.local.StockDatabase
import com.realityexpander.stockmarketapp.data.mapper.toCompanyInfo
import com.realityexpander.stockmarketapp.data.mapper.toCompanyListing
import com.realityexpander.stockmarketapp.data.remote.dto.CompanyInfoDTO
import com.realityexpander.stockmarketapp.data.remote.dto.StockApi
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import com.realityexpander.stockmarketapp.util.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class StockRepositoryImplTest {

    // Setup fresh remote data with 100 companies
    private val freshCompanyListingsFromRemote = (1..100).map {
        CompanyListing(
            companyName = "name$it",
            companySymbol = "symbol$it",
            companyExchange = "exchange$it"
        )
    }

    // Setup fresh remote data with 100 intraday infos
    private val freshIntradayInfosFromRemote = (1..100).map {
        IntradayInfo(
            datetime = LocalDateTime.now(),
            close = it.toDouble(),
            open = it.toDouble(),
            high = it.toDouble(),
            low = it.toDouble(),
            volume = it * 100
        )
    }

    private lateinit var repository: StockRepositoryImpl
    private lateinit var noOpStockApiMockk: StockApi
    private lateinit var stockDatabaseMockk: StockDatabase
    private lateinit var stockDaoFake: StockDao
    private lateinit var companyListingsCSVParserMockk: CSVParser<CompanyListing>
    private lateinit var intradayInfosCSVParserMockk: CSVParser<IntradayInfo>

    @Before
    fun setUp() {

        // Mock the StockApi - getListOfStocks() returns a stub that is a no-op
        noOpStockApiMockk = mockk(relaxed = true) {
            coEvery { getListOfStocks(any()) } returns mockk(relaxed = true)
            coEvery { getIntradayInfo(any()) } returns mockk(relaxed = true)
        }

        // Fake the DAO - Simulates the DAO calls to the database
        stockDaoFake = StockDaoFake()

        // Mock the StockDatabase - .dao returns the DAO Fake
        stockDatabaseMockk = mockk(relaxed = true) {
            every { dao } returns stockDaoFake
        }

        // Mock the CSVParser<CompanyListing> - parse() returns CompanyListing objects
        companyListingsCSVParserMockk = mockk(relaxed = true) {
            coEvery { parse(any()) } returns freshCompanyListingsFromRemote
        }

        // Mock the CSVParser<IntradayInfo> - parse() returns IntradayInfos objects
        intradayInfosCSVParserMockk = mockk(relaxed = true) {
            coEvery { parse(any()) } returns freshIntradayInfosFromRemote
        }

        // Create object under test with above dependencies
        repository = StockRepositoryImpl(
            api = noOpStockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )
    }

    @Test
    // 1. DB has a stale date (1 item).
    // 2. Remote fetch gets 100 fresh items.
    // 3. DB is cleared of all items and newly fetched items are inserted.
    // 4. DB emits the 100 fresh items (and no stale items)
    fun `Local database cache will be overwritten with remote data when fetch = true`() = runTest {

        // ARRANGE
        // Setup stale database data - Insert 1 item into the DB (stale data)
        val staleCompanyListings =
            listOf(
                CompanyListingEntity(
                    name = "test-name",
                    symbol = "test-symbol",
                    exchange = "test-exchange",
                    id = 0
                )
            )
        stockDaoFake.insertCompanyListings(staleCompanyListings)

        // ACT/ASSERT - check the flow of flow emissions from the repository
        repository.getCompanyListings(
            fetchFromRemote = true,
            query = ""
        ).test {

            // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)

            val startLoading = awaitItem()
            assertThat((startLoading as Resource.Loading).isLoading).isTrue()

            val staleCompanyListingsInDB = awaitItem()
            assertThat(staleCompanyListingsInDB is Resource.Success).isTrue()
            assertThat(
                    staleCompanyListingsInDB.data
                ).isEqualTo(
                    staleCompanyListings.map {
                        it.toCompanyListing()
                    }
                )

            val freshCompanyListingsInDB = awaitItem()
            assertThat(freshCompanyListingsInDB is Resource.Success).isTrue()
            assertThat(
                    stockDaoFake.searchCompanyListing("").map {
                        it.toCompanyListing()
                    }
                ).isEqualTo(
                    freshCompanyListingsFromRemote
            )

            val stopLoading = awaitItem()
            assertThat((stopLoading as Resource.Loading).isLoading).isFalse()

            awaitComplete()
        }
    }

    @Test
    // 1. Remote fetch gets 100 fresh items.
    // 2. Response is Success and item count is 100.
    fun `getIntradayInfos will fetched from remote api successfully`() = runTest {

        // ARRANGE
        /* uses default arrangement */

        // ACT
        val apiResponse = repository.getIntradayInfos("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Success::class.java)
        assertThat((apiResponse as Resource.Success).data).isNotNull()
        (apiResponse as Resource.Success).data?.let { intradayInfos ->
            assertThat(intradayInfos).hasSize(100)
            assertThat(intradayInfos).isEqualTo(freshIntradayInfosFromRemote)
        }
    }

    @Test
    // 1. Remote fetch fails with exception.
    // 2. Response is Error and has error message.
    fun `getIntradayInfos will fail due to fetch error`() = runTest {

        // ARRANGE
        // Mock the StockApi - getIntradayInfos() throws an exception
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getIntradayInfo(any()) } throws Exception("test api error")
        }

        // Create object under test with above dependencies
        repository = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repository.getIntradayInfos("UNKNOWN_COMPANY_SYMBOL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
        assertThat((apiResponse as Resource.Error).message).isEqualTo("test api error")
    }

    @Test
    // 1. Parsing CSV fails with exception.
    // 2. Response is Error and has error message.
    fun `getIntradayInfos will fail due to parsing error`() = runTest {

        // ARRANGE
        // Mock the StockApi - parse() throws an exception
        val intradayInfosCSVParserMockk: CSVParser<IntradayInfo> = mockk(relaxed = true) {
            coEvery { parse(any()) } throws Exception("test parse error")
        }

        // Create object under test with above dependencies
        repository = StockRepositoryImpl(
            api = noOpStockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repository.getIntradayInfos("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
        assertThat((apiResponse as Resource.Error).message).isEqualTo("test parse error")
    }

    @Test
    // 1. Remote fetch succeeds fetching a companyInfo object.
    // 2. Response is Error and has error message = "API limit reached, please try again later."
    fun `getCompanyInfo will fetch data successfully`() = runTest {

        // ARRANGE
        val expectedCompanyInfo = CompanyInfoDTO(
            companyName = "Company Name",
            exchange = "NASDAQ",
            country = "US",
            description = "This is the test description",
            industry = "Technology",
            symbol = "GOOGL",
        )

        // Mock the StockApi - getIntradayInfos() throws an exception
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getCompanyInfo(any()) } returns expectedCompanyInfo
        }

        // Create object under test with above dependencies
        repository = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repository.getCompanyInfo("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Success::class.java)
        (apiResponse as Resource.Success).data?.let { companyInfo ->
            assertThat(companyInfo).isEqualTo(expectedCompanyInfo.toCompanyInfo())
        }
    }

    @Test
    // 1. Remote fetch fails with error due to API Limit hit.
    // 2. Response is Error and has correct error message
    fun `getCompanyInfo will fail due to fetch error - API Limit reached`() = runTest {

        // ARRANGE
        val expectedErrorMessage = "API limit reached, please try again later."

        // Mock the StockApi - getIntradayInfos() throws an exception
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getCompanyInfo(any()) } returns CompanyInfoDTO(
                companyName = null,
                exchange = null,
                country = null,
                description = null,
                industry = null,
                symbol = null,
            )
        }

        // Create object under test with above dependencies
        repository = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repository.getCompanyInfo("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
        assertThat((apiResponse as Resource.Error).message).isEqualTo(expectedErrorMessage)
    }

}


















































