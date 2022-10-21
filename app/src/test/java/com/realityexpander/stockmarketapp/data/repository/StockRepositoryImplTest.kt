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
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.LocalDateTime
import kotlin.reflect.cast
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*

// Articles on testing:
// https://medium.com/swlh/kotlin-coroutines-in-android-unit-test-28ff280fc0d5

// Testing livedata & other android components, dispatchers.
// https://medium.com/swlh/unit-testing-with-kotlin-coroutines-the-android-way-19289838d257



@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
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

    private lateinit var repositoryTest: StockRepositoryImpl
    private lateinit var noOpStockApiMockk: StockApi
    private lateinit var stockDatabaseMockk: StockDatabase
    private lateinit var stockDaoFake: StockDao
    private lateinit var spyStockDaoFake: StockDao
    private lateinit var companyListingsCSVParserMockk: CSVParser<CompanyListing>
    private lateinit var intradayInfosCSVParserMockk: CSVParser<IntradayInfo>

    //@get:Rule
    //var instantExecutorRule = InstantTaskExecutorRule()

    //private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        //MockKAnnotations.init(this)
        //Dispatchers.setMain(testDispatcher)

        // Mock the StockApi - getListOfStocks() returns a stub that is a no-op
        noOpStockApiMockk = mockk(relaxed = true) {
            coEvery { getListOfStocksRawCSV(any()) } returns mockk(relaxed = true)
            coEvery { getIntradayInfoRawCSV(any()) } returns mockk(relaxed = true)
        }

        // Fake the DAO - Simulates the DAO calls to the database
        stockDaoFake = StockDaoFake()

        // setup spy on the Fake DAO
        spyStockDaoFake = spyk(stockDaoFake)

        // Mock the StockDatabase - .dao returns the DAO Fake
        stockDatabaseMockk = mockk(relaxed = true) {
            every { dao } returns spyStockDaoFake
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
        repositoryTest = StockRepositoryImpl(
            api = noOpStockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )
    }

    @Test
    // 1. DB has stale data (1 item).
    // 2. Remote fetch gets 100 fresh items.
    // 3. DB is cleared of all items and newly fetched items are inserted.
    // 4. DB emits the 100 fresh items (and no stale items)
    fun `Local database cache will be overwritten with fresh remote data when fetch = true`() = runTest {

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
        spyStockDaoFake.insertCompanyListings(staleCompanyListings)

        // setup spy on the repository under Test
        val spyRepositoryTest = spyk(repositoryTest)

        // ACT/ASSERT - check the flow of flow emissions from the repository
        spyRepositoryTest.getCompanyListings(
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
                spyStockDaoFake.searchCompanyListing("").map {
                    it.toCompanyListing()
                }
            ).isEqualTo(
                freshCompanyListingsFromRemote
            )

            val stopLoading = awaitItem()
            assertThat((stopLoading as Resource.Loading).isLoading).isFalse()

            awaitComplete()
            //cancelAndIgnoreRemainingEvents()
            //cancelAndConsumeRemainingEvents()

            coVerify(exactly = 1) { spyRepositoryTest.getCompanyListings(any(), any()) }
            coVerify(exactly = 3) { spyStockDaoFake.searchCompanyListing(any()) }
        }
    }

    @Test
    // 1. DB has stale data (1 item).
    // 2. DB emits the 1 stale item.
    fun `Local database cache stale data will be returned when fetch = false`() = runTest {

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
        spyStockDaoFake.insertCompanyListings(staleCompanyListings)

        // setup spy on the repository under Test
        val spyRepositoryTest = spyk(repositoryTest)

        // ACT
        spyRepositoryTest.getCompanyListings(
            fetchFromRemote = false,
            query = ""
        ).test {

            // ASSERT - check the flow of flow emissions from the repository
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

            val stopLoading = awaitItem()
            assertThat((stopLoading as Resource.Loading).isLoading).isFalse()

            awaitComplete()

            coVerify(exactly = 1) { spyRepositoryTest.getCompanyListings(any(), any()) }
            coVerify(exactly = 1) { spyStockDaoFake.searchCompanyListing(any()) }
        }
    }

    @Test
    // 1. Remote fetch gets 100 fresh items.
    // 2. Response is Success and item count is 100.
    fun `getIntradayInfos will fetched from remote api successfully`() = runTest {

        // ARRANGE
        /* uses default arrangement */

        // ACT
        val apiResponse = repositoryTest.getIntradayInfos("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Success::class.java)
        assertThat((apiResponse as Resource.Success).data).isNotNull()
        (apiResponse).data?.let { intradayInfos ->
            assertThat(intradayInfos).hasSize(100)
            assertThat(intradayInfos).isEqualTo(freshIntradayInfosFromRemote)
        }
    }

    @Test
    // 1. IntradayInfosCSVParser fails with IOException (API limit reached).
    // 2. Response is Error and has error message.
    @Suppress("useless_cast")
    fun `getIntradayInfos will fail due to IntradayInfosCSVParser IOException`() = runTest {

        // ARRANGE
        val expectedException = IOException("Test - API LIMIT REACHED")

        // CSVParser<IntradayInfo> - parse() returns exception
        intradayInfosCSVParserMockk = mockk(relaxed = true) {
            coEvery { parse(any()) } throws expectedException
        }

        // Create object under test with above dependencies
        repositoryTest = StockRepositoryImpl(
            api = noOpStockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repositoryTest.getIntradayInfos("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
        assertThat((apiResponse as Resource.Error).message).isNotNull()
        assertThat((apiResponse as Resource.Error).message).isEqualTo(expectedException.localizedMessage)
        coVerify(exactly = 1) { intradayInfosCSVParserMockk.parse(any()) }
    }

    @Test
    // 1. IntradayInfosCSVParser fails with IOException (API limit reached).
    // 2. Exception contains error message.
    fun `getIntradayInfosWithoutCatches will fail due to IntradayInfosCSVParser IOException`() = runTest {

        // ARRANGE
        val expectedException = IOException("Test - API LIMIT REACHED")

        // CSVParser<IntradayInfo> - parse() returns exception
        intradayInfosCSVParserMockk = mockk(relaxed = true) {
            coEvery { parse(any()) } throws expectedException
        }

        // Create object under test with above dependencies
        repositoryTest = StockRepositoryImpl(
            api = noOpStockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        if (true) {
            // ACT
            val apiRes = runCatching {
                repositoryTest.getIntradayInfosWithoutCatches("GOOGL")

                assertThat(true).isFalse() // should not reach this line
            }.onFailure {
                // ASSERT
                assertThat(it).isInstanceOf(expectedException::class.java)
            }

            // ASSERT
            assertThat(apiRes.isFailure).isTrue()
            assertThat(apiRes.exceptionOrNull()).isInstanceOf(expectedException::class.java)
            assertThat(apiRes.exceptionOrNull()?.message).isEqualTo(expectedException.localizedMessage)
        }

        // Alternate method of detecting exception
        if (false) {
            try {
                // ACT
                val apiResponse = repositoryTest.getIntradayInfosWithoutCatches("UNKNOWN_COMPANY_SYMBOL")

                assertThat(true).isFalse() // should not reach this line
                println("apiResponse = $apiResponse")
            } catch (e: Throwable) {
                // ASSERT
                assertThat(e).isInstanceOf(expectedException::class.java)
                assertThat(e.message).isEqualTo(expectedException.localizedMessage)
            }
        }

        // ASSERT
        coVerify { intradayInfosCSVParserMockk.parse(any()) }
    }

    @Test
    // 1. Remote fetch fails with HttpException. ***
    // 2. Response is Error and has error message.
    fun `getIntradayInfos will fail due to fetch HttpException`() {

        // ARRANGE
        val expectedExceptionCode = 404
        val expectedException = HttpException(
            Response.error<String>(
                /* code = */
                expectedExceptionCode,
                /* body = */
                "".toResponseBody(contentType = "text/plain".toMediaTypeOrNull()),
            )
        )

        // getIntradayInfos() throws an HttpException
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getIntradayInfoRawCSV(any()) } coAnswers {
                throw expectedException
            }
        }

        // Create object under test with above dependencies
        repositoryTest = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        runTest {
//        runBlocking {

            // ACT
            val apiResponse = repositoryTest.getIntradayInfos("UNKNOWN_COMPANY_SYMBOL")
//            val apiResponse = Resource.Error<String>("HTTP 404 Response.error()")

            //ASSERT
            // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
            assertThat(apiResponse).isNotNull()
            assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
            assertThat((apiResponse as Resource.Error).message).isNotNull()
            @Suppress("useless_cast")
            assertThat((apiResponse as Resource.Error).message).isEqualTo(expectedException.localizedMessage)
            coVerify { stockApiMockk.getIntradayInfoRawCSV(any()) }
        }
    }

    @Test
    // 1. Remote fetch fails with general Exception.
    // 2. Response is Error and has error message.
    fun `getIntradayInfos will fail due to fetch general exception`() = runTest {

        // ARRANGE

        // General Exception
        val expectedException = Exception("Test Exception")

        // getIntradayInfos() throws an exception
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getIntradayInfoRawCSV(any()) } coAnswers {
                throw expectedException
            }
        }

        // Create object under test with above dependencies
        repositoryTest = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repositoryTest.getIntradayInfos("UNKNOWN_COMPANY_SYMBOL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
        assertThat((apiResponse as Resource.Error).message).isNotNull()
        @Suppress("useless_cast")
        assertThat((apiResponse as Resource.Error).message).isEqualTo(expectedException.localizedMessage)
        coVerify { stockApiMockk.getIntradayInfoRawCSV(any()) }
    }

    @Test
    // 1. Remote fetch fails with IOException.
    // 2. Response is Error and has error message.
    @Suppress("useless_cast")
    fun `getIntradayInfos will fail due to fetch IOException`() = runTest {

        // ARRANGE

        // IOException
        val expectedException = IOException("Test IOException")

        // getIntradayInfos() throws an exception
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getIntradayInfoRawCSV(any()) } coAnswers {
                throw expectedException
            }
        }

        // Create object under test with above dependencies
        repositoryTest = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repositoryTest.getIntradayInfos("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
        assertThat((apiResponse as Resource.Error).message).isNotNull()
        assertThat((apiResponse as Resource.Error).message).isEqualTo(expectedException.localizedMessage)
        coVerify { stockApiMockk.getIntradayInfoRawCSV(any()) }
    }

    @Test
    // 1. Remote fetch fails with HttpException and NOT caught in repository.
    // 2. Exception contains error message.
    fun `getIntradayInfosWithoutCatches will fail due to fetch HttpException`() = runTest {

        // ARRANGE
        val expectedExceptionCode = 404
        val expectedException = HttpException(
            Response.error<String>(
                /* code = */ expectedExceptionCode,
                /* body = */ "".toResponseBody(contentType = "text/plain".toMediaTypeOrNull()),
            )
        )

        // getIntradayInfos() throws an exception
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getIntradayInfoRawCSV(any()) } coAnswers {
                throw expectedException
            }
        }

        // Create object under test
        repositoryTest = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        if (true) {
            // ACT
            val apiRes = runCatching {
                repositoryTest.getIntradayInfosWithoutCatches("UNKNOWN_COMPANY_SYMBOL")

                assertThat(true).isFalse() // should not reach this line
            }.onFailure {
                // ASSERT
                assertThat(it).isInstanceOf(expectedException::class.java)
            }

            // ASSERT
            assertThat(apiRes.isFailure).isTrue()
            assertThat(apiRes.exceptionOrNull()).isInstanceOf(expectedException::class.java)
            assertThat(apiRes.exceptionOrNull()?.message).isEqualTo(expectedException.localizedMessage)

            //assertThat((apiRes.exceptionOrNull() as HttpException).code()).isEqualTo(expectedExceptionCode)
            assertThat((expectedException::class).cast(apiRes.exceptionOrNull()) // Force to the exception type
                        .code()
                ).isEqualTo(expectedExceptionCode)
        }

        // Alternate method of detecting exception
        if (false) {
            try {
                // ACT
                val apiResponse = repositoryTest.getIntradayInfosWithoutCatches("UNKNOWN_COMPANY_SYMBOL")

                assertThat(true).isFalse() // should not reach this line
                println("apiResponse: $apiResponse")
            } catch (e: Throwable) {
                // ASSERT
                assertThat(e).isInstanceOf(expectedException::class.java)
                assertThat(e.message).isEqualTo(expectedException.localizedMessage)
                //assertThat((e as HttpException).code()).isEqualTo(expectedExceptionCode)
                assertThat((expectedException::class).cast(e) // Force to the exception type
                        .code()
                    ).isEqualTo(expectedExceptionCode)
            }

            // ASSERT
            coVerify { stockApiMockk.getIntradayInfoRawCSV(any()) }
        }
    }

    @Test
    // 1. Remote fetch fails with IOException and NOT caught in repository.
    // 2. Exception contains error message.
    fun `getIntradayInfosWithoutCatches will fail due to fetch IOException`() = runTest {

        // ARRANGE
        val expectedException = IOException("Test IOException")

        // getIntradayInfos() throws an exception
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getIntradayInfoRawCSV(any()) } coAnswers {
                throw expectedException
            }
        }

        // Create object under test
        repositoryTest = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        if (true) {
            // ACT
            val apiRes = runCatching {
                repositoryTest.getIntradayInfosWithoutCatches("UNKNOWN_COMPANY_SYMBOL")

                assertThat(true).isFalse() // should not reach this line
            }.onFailure {
                // ASSERT
                assertThat(it).isInstanceOf(expectedException::class.java)
            }

            // ASSERT
            assertThat(apiRes.isFailure).isTrue()
            assertThat(apiRes.exceptionOrNull()).isInstanceOf(expectedException::class.java)
            assertThat(apiRes.exceptionOrNull()?.message).isEqualTo(expectedException.localizedMessage)
        }

        // Alternate method of detecting exception
        if (false) {
            try {
                // ACT
                val apiResponse = repositoryTest.getIntradayInfosWithoutCatches("UNKNOWN_COMPANY_SYMBOL")

                assertThat(true).isFalse() // should not reach this line
                println("apiResponse: $apiResponse")
            } catch (e: Throwable) {
                // ASSERT
                assertThat(e).isInstanceOf(expectedException::class.java)
                assertThat(e.message).isEqualTo(expectedException.localizedMessage)
            }

            // ASSERT
            coVerify { stockApiMockk.getIntradayInfoRawCSV(any()) }
        }
    }

    @Test
    // 1. Remote fetch fails with General Exception and NOT caught in repository.
    // 2. Exception contains error message.
    fun `getIntradayInfosWithoutCatches will fail due to fetch General Exception`() = runTest {

        // ARRANGE
        val expectedException = Exception("Test Exception")

        // getIntradayInfos() throws an exception
        val stockApiMockk: StockApi = mockk(relaxed = true) {
            coEvery { getIntradayInfoRawCSV(any()) } coAnswers {
                throw expectedException
            }
        }

        // Create object under test
        repositoryTest = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        if (true) {
            // ACT
            val apiRes = runCatching {
                repositoryTest.getIntradayInfosWithoutCatches("UNKNOWN_COMPANY_SYMBOL")

                assertThat(true).isFalse() // should not reach this line
            }.onFailure {
                // ASSERT
                assertThat(it).isInstanceOf(expectedException::class.java)
            }

            // ASSERT
            assertThat(apiRes.isFailure).isTrue()
            assertThat(apiRes.exceptionOrNull()).isInstanceOf(expectedException::class.java)
            assertThat(apiRes.exceptionOrNull()?.message).isEqualTo(expectedException.message)
        }

        // Alternate method of detecting exception
        if (false) {
            try {
                // ACT
                val apiResponse = repositoryTest.getIntradayInfosWithoutCatches("UNKNOWN_COMPANY_SYMBOL")

                assertThat(true).isFalse() // should not reach this line
                println("apiResponse: $apiResponse")
            } catch (e: Throwable) {
                // ASSERT
                assertThat(e).isInstanceOf(expectedException::class.java)
                assertThat(e.message).isEqualTo(expectedException.localizedMessage)
            }

            // ASSERT
            coVerify { stockApiMockk.getIntradayInfoRawCSV(any()) }
        }
    }

    @Test
    // 1. Parsing CSV fails with exception.
    // 2. Response is Error and has error message.
    fun `getIntradayInfos will fail due to parsing error`() = runTest {

        // ARRANGE
        // parse() throws an exception
        val expectedException = Exception("Test Exception")
        val intradayInfosCSVParserMockk: CSVParser<IntradayInfo> = mockk(relaxed = true) {
            coEvery { parse(any()) } throws expectedException
        }

        // Create object under test with above dependencies
        repositoryTest = StockRepositoryImpl(
            api = noOpStockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repositoryTest.getIntradayInfos("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
        assertThat((apiResponse as Resource.Error).message).isEqualTo(expectedException.message)
    }

    @Test
    // 1. Remote fetch succeeds fetching a companyInfo object.
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
        repositoryTest = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repositoryTest.getCompanyInfo("GOOGL")

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

        // getCompanyInfo() returns null data
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
        repositoryTest = StockRepositoryImpl(
            api = stockApiMockk,
            db = stockDatabaseMockk,
            companyListingsCSVParser = companyListingsCSVParserMockk,
            intradayInfoCSVParser = intradayInfosCSVParserMockk
        )

        // ACT
        val apiResponse = repositoryTest.getCompanyInfo("GOOGL")

        //ASSERT
        // should use format: assertThat(ACTUAL).isEqualTo(EXPECTED)
        assertThat(apiResponse).isNotNull()
        assertThat(apiResponse).isInstanceOf(Resource.Error::class.java)
        assertThat((apiResponse as Resource.Error).message).isEqualTo(expectedErrorMessage)
    }

}


















































