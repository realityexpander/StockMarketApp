package com.realityexpander.stockmarketapp.presentation.company_info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.realityexpander.stockmarketapp.ui.theme.DarkBlue

@Composable
@Destination
fun CompanyInfoScreen(
    symbol: String,
    viewModel: CompanyInfoViewModel = hiltViewModel()
) {
    val state = viewModel.state

    if (state.errorMessageCompanyInfo == null && state.companyInfo != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBlue)
                .padding(16.dp)
        ) {
            state.companyInfo.let { company ->
                Text(
                    text = company.companyName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = company.symbol,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Industry: ${company.industry}",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Country: ${company.country}",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Description:",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = company.description,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Market Summary for " +
                            if (state.intradayInfos.isEmpty()) "today" else {
                                state.intradayInfos.first().datetime.month.name.toTitlecase() + " " +
                                        state.intradayInfos.first().datetime.dayOfMonth.toString() + ", " +
                                        state.intradayInfos.first().datetime.year.toString()
                            },
                )
                Spacer(modifier = Modifier.height(32.dp))
                if (state.intradayInfos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    StockChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .align(CenterHorizontally),
                        infos = state.intradayInfos,
                    )
                }
                if (state.intradayInfos.isEmpty() && !state.isLoadingIntradayInfos) {
                    Text(
                        text = "Data not available.",
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Center
    ) {
        if (state.isLoadingIntradayInfos || state.isLoadingCompanyInfo) {
            CircularProgressIndicator()
        }

        // Show error message if any
        Spacer(modifier = Modifier.height(32.dp))
        if (state.errorMessageCompanyInfo != null && !state.isLoadingCompanyInfo) {
            Text(
                text = state.errorMessageCompanyInfo,
                color = MaterialTheme.colors.error
            )
        } else {
            if (state.errorMessageIntradayInfos != null && !state.isLoadingIntradayInfos) {
                Text(
                    text = state.errorMessageIntradayInfos,
                    color = MaterialTheme.colors.error
                )
            }
        }
    }
}

// Make first char uppercase and rest lowercase
private fun String.toTitlecase(): String {
    return this.lowercase()
        .split(" ")
        .joinToString("") { string ->
            string.replaceFirstChar { firstChar ->
                firstChar.uppercaseChar()
            }
        }
}
