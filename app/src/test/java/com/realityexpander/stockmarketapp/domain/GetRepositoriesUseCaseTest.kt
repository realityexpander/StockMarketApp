package com.realityexpander.stockmarketapp.domain

import com.realityexpander.stockmarketapp.data.GithubRepository
import com.realityexpander.stockmarketapp.data.ApiRepositoryModel
import com.realityexpander.stockmarketapp.data.RepositoryModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetRepositoriesUseCaseTest {

    @MockK
    lateinit var githubRepository: GithubRepository

    lateinit var getRepositoriesUseCase: GetRepositoriesUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        this.getRepositoriesUseCase = GetRepositoriesUseCase(githubRepository)
    }

    @Test
    fun testExecutePositive() = runBlocking {
        coEvery { githubRepository.fetchRepositories("irtizakh") } returns listOf(
            ApiRepositoryModel(
                "1",
                "mockito"
            ), ApiRepositoryModel("2", "rxjava")
        )

        val list = getRepositoriesUseCase.execute("irtizakh")

        assertNotNull(list)
        assert(list[0] is RepositoryModel)
        assertEquals("1", list[0].repositoryID)
        assertEquals("mockito", list[0].repositoryName)
        assertEquals("2", list[1].repositoryID)
        assertEquals("rxjava", list[1].repositoryName)
    }

    @Test
    fun testExecuteNegative() = runBlocking {
        coEvery { githubRepository.fetchRepositories("irtizakh") } throws IllegalStateException("error")
        try {
            val list = getRepositoriesUseCase.execute("irtizakh")
        } catch (e: Exception) {
            assert(e is IllegalStateException)
            assertEquals("error", e.message)
        }
    }

}