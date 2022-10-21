package com.realityexpander.stockmarketapp.domain

import com.realityexpander.stockmarketapp.data.GithubRepository
import com.realityexpander.stockmarketapp.data.RepositoryModel

class GetRepositoriesUseCase(private val apiRepository: GithubRepository) {
    suspend fun execute(username: String): List<RepositoryModel> {
        return this.apiRepository.fetchRepositories(username).map {
            RepositoryModel(it.id, it.name)
        }
    }
}