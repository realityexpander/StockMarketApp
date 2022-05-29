package com.realityexpander.stockmarketapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_listing_entity")
data class CompanyListingEntity(
    val name: String = "",
    val symbol: String = "",
    val exchange: String = "",

    @PrimaryKey
    var id: Int? = null,
)