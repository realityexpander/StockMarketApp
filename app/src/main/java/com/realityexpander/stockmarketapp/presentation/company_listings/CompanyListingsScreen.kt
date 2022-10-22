package com.realityexpander.stockmarketapp.presentation.company_listings

import android.widget.ProgressBar
import android.widget.Spinner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.realityexpander.stockmarketapp.domain.model.CompanyListing
import com.realityexpander.stockmarketapp.presentation.destinations.CompanyInfoScreenDestination
import com.realityexpander.stockmarketapp.util.Resource
import kotlinx.coroutines.delay

@Composable
@Destination(start = true)
fun CompanyListingsScreen(
    navigator: DestinationsNavigator,
    viewModel: CompanyListingsViewModel = hiltViewModel()
) {
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = viewModel.state.isRefreshing
    )
    val state = viewModel.state

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = {
                viewModel.onEvent(CompanyListingsEvent.OnSearchQueryChanged(it))
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            placeholder = {
                Text("Search…")
            },
            maxLines = 1,
            singleLine = true,
        )

        // Show loading indicator
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(50.dp),
                color = Color.White
            )
            Text(
                "Loading…",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Show error message
        if (state.errorMessage != null) {
            Text(
                "Error: ${state.errorMessage}",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                viewModel.onEvent(CompanyListingsEvent.OnRefresh)
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.companyListings.size) { i ->
                    CompanyListingItem(
                        companyListing = state.companyListings[i],
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navigator.navigate(
                                    CompanyInfoScreenDestination(
                                        symbol = state.companyListings[i].companySymbol
                                    )
                                )
                            },
                    )

                    if (i < state.companyListings.size) { // dont show divider for last item
                        Divider(
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                    }
                }
            }
        }
    }

}
