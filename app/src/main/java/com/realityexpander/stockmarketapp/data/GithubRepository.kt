package com.realityexpander.stockmarketapp.data

import com.realityexpander.stockmarketapp.data.ApiRepositoryModel


interface GithubRepository {

    suspend fun fetchRepositories(username: String) : List<ApiRepositoryModel>

}