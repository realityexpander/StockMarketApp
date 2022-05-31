package com.realityexpander.stockmarketapp.presentation.company_info

import com.realityexpander.stockmarketapp.domain.model.CompanyInfo
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockIntradayInfos: List<IntradayInfo> = emptyList(),
    val companyInfo: CompanyInfo? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
