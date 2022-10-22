package com.realityexpander.stockmarketapp.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.realityexpander.stockmarketapp.data.RepositoryModel
import com.realityexpander.stockmarketapp.data.csv.CSVParser
import com.realityexpander.stockmarketapp.data.remote.dto.CompanyInfoDTO
import com.realityexpander.stockmarketapp.data.remote.dto.StockApi
import com.realityexpander.stockmarketapp.domain.GetRepositoriesUseCase
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import com.realityexpander.stockmarketapp.util.Resource
import com.realityexpander.stockmarketapp.utils.LiveDataResult
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import retrofit2.HttpException
import java.io.InputStream
import java.io.Reader
import java.time.LocalDateTime

class MainViewModel(
    mainDispatcher : CoroutineDispatcher,
    ioDispatcher : CoroutineDispatcher,
    private val getRepositoriesUseCase: GetRepositoriesUseCase,
    val api: StockApi = StockApiFake()
    ) : ViewModel() {

        private val job = SupervisorJob()

        val repositoriesLiveData = MutableLiveData<LiveDataResult<List<RepositoryModel>>>()

        private val uiScope = CoroutineScope(mainDispatcher + job)

        val ioScope = CoroutineScope(ioDispatcher + job)

        fun fetchRepositories(user: String) {
            uiScope.launch {
                repositoriesLiveData.value = LiveDataResult.loading()

                try {
                    val data = ioScope.async {
                        return@async getRepositoriesUseCase.execute(user)
                    }.await()

                    repositoriesLiveData.value = LiveDataResult.success(data)
                } catch (e: Exception) {
                    repositoriesLiveData.value = LiveDataResult.error(e)
                }

            }

        }

        override fun onCleared() {
            super.onCleared()
            this.job.cancel()
        }

        suspend fun getIntradayInfos(stockSymbol: String): Resource<List<IntradayInfo>> {
            val parser = IntradayInfoCSVParserFake()

            return try {
                val response = api.getIntradayInfoRawCSV(stockSymbol)
                // println(response.readBytes().toString(Charsets.UTF_8)) // keep for debugging

                val results = parser.parse(response.byteStream())
                Resource.Success(results)
            } catch (e: IOException) { // parse error
                //e.printStackTrace()
                Resource.Error(e.localizedMessage ?: "Error loading or parsing intraday info")
            } catch (e: HttpException) { // invalid network response
                //e.printStackTrace()
                Resource.Error(e.localizedMessage ?: "Error with network for intraday info")
            } catch (e: Exception) { // unknown error
                //e.printStackTrace()
                Resource.Error(e.localizedMessage ?: "Unknown Error loading or parsing intraday info")
            }
        }
}

class StockApiFake: StockApi {
    override suspend fun getListOfStocksRawCSV(apiKey: String): ResponseBody {
        return ("timestamp,open,high,low,close,volume" + System.lineSeparator() +
                "2021-01-01,1.0,2.0,3.0,4.0,5.0" + System.lineSeparator() +
                "2021-01-02,1.0,2.0,3.0,4.0,5.0" + System.lineSeparator()).toResponseBody("text/csv".toMediaTypeOrNull())
    }

    override suspend fun getIntradayInfoRawCSV(symbol: String, apiKey: String): ResponseBody {
        return ("timestamp,open,high,low,close,volume" + System.lineSeparator() +
                "2021-01-01,1.0,2.0,3.0,4.0,5.0" + System.lineSeparator() +
                "2021-01-02,1.0,2.0,3.0,4.0,5.0" + System.lineSeparator()).toResponseBody("text/csv".toMediaTypeOrNull())
    }

    override suspend fun getCompanyInfo(symbol: String, apiKey: String): CompanyInfoDTO {
        return CompanyInfoDTO(
            symbol = "TEST",
            companyName = "Test Company",
            exchange = "Test Exchange",
            industry = "Test Industry",
            description = "Test Description",
            country = "Test Country",
        )
    }
}

class IntradayInfoCSVParserFake: CSVParser<IntradayInfo> {
    override suspend fun parse(csvStream: InputStream): List<IntradayInfo> {
        return listOf(
            IntradayInfo(LocalDateTime.parse("2021-01-01T00:00:00"), 1.0, 2.0, 3.0, 4.0, 5000),
            IntradayInfo(LocalDateTime.parse("2021-02-01T01:00:30"), 2.0, 3.0, 4.0, 5.0, 6000),
        )
    }
}
