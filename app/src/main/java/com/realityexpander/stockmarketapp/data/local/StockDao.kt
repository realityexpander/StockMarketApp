package com.realityexpander.stockmarketapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StockDao {

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    suspend fun insertCompanyListings(companyListingEntities: List<CompanyListingEntity>)

    @Query("DELETE FROM company_listing_entity")
    suspend fun clearCompanyListings()

    // "||" is like string "+" (concatenation) in kotlin
    // tEs -> name LIKE %tes% OR TES == symbol
    @Query(
        """
            SELECT * 
            FROM company_listing_entity 
            WHERE LOWER(name) LIKE '%' || LOWER(:searchString) || '%' OR
                UPPER(:searchString) == symbol
        """
    )
    suspend fun searchCompanyListing(searchString: String): List<CompanyListingEntity>
}