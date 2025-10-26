package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.result.TokenResult
import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.TokenProviderPort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class OAuthAuthenticateSuccessServiceTest {

    private lateinit var tokenProvider: TokenProviderPort
    private lateinit var cachePort: CachePort
    private lateinit var service: OAuthAuthenticateSuccessService

    @BeforeEach
    fun setUp() {
        tokenProvider = mock()
        cachePort = mock()
        service = OAuthAuthenticateSuccessService(tokenProvider, cachePort)
    }

    @Test
    fun `handleSuccess generates token and caches refresh token`() {
        val username = "user@test.com"
        val roles = listOf("ROLE_USER")
        val tokenResult = TokenResult("type", "access", "refresh", 600L, 3600L)

        whenever(tokenProvider.generateToken(username, roles.joinToString("|"))).thenReturn(tokenResult)

        val result = service.handleSuccess(username, roles)

        assertEquals("access", result.accessToken)
        assertEquals("refresh", result.refreshToken)
        verify(cachePort).save(username, "refresh", 3600L)
    }
}