package com.realityexpander.stockmarketapp.presentation.company_listings

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realityexpander.stockmarketapp.domain.model.CompanyListing

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun CompanyListingPreview() {
//    CompanyListingItem(
//        companyListing = CompanyListing(
//            companySymbol = "AAPL",
//            companyName = "Apple Inc.",
//            companyExchange = "NASDAQ",
//        )
//    )
//}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun TextPreview() {
//    Text(
//        text = "Hello World",
////        modifier = Modifier.fillMaxWidth()
//    )
//}


@Composable
fun CompanyListingItem(
    companyListing: CompanyListing,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = companyListing.companyName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onBackground,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = companyListing.companyExchange,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colors.onBackground,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "(${companyListing.companySymbol})",
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                color = MaterialTheme.colors.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}































