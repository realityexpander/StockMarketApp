package com.realityexpander.stockmarketapp.presentation.company_info

import com.realityexpander.stockmarketapp.domain.model.CompanyInfo
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockIntradayInfos: List<IntradayInfo> = emptyList(),
    val companyInfo: CompanyInfo? = null,
    val isLoadingStockIntradayInfos: Boolean = false,
    val isLoadingCompanyInfo: Boolean = false,
    val errorMessageStockIntradayInfos: String? = null,
    val errorMessageCompanyInfo: String? = null
)
