package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.result.PrincipalResult
import io.brieflyz.auth_service.application.dto.result.TokenResult
import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.TokenProviderPort
import io.brieflyz.auth_service.common.exception.RefreshTokenNotFoundException
import io.brieflyz.core.constants.Role
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TokenRefreshServiceTest {

    private lateinit var tokenProvider: TokenProviderPort
    private lateinit var cachePort: CachePort
    private lateinit var service: TokenRefreshService

    @BeforeEach
    fun setUp() {
        tokenProvider = mock()
        cachePort = mock()
        service = TokenRefreshService(tokenProvider, cachePort)
    }

    @Test
    fun `refresh token successfully`() {
        val username = "user@test.com"
        val refreshToken = "refresh-token"
        val principalResult = PrincipalResult(username, listOf(Role.USER.name))
        val tokenResult = TokenResult("TokenType", "A", "R", 600L, 3600L)

        whenever(cachePort.exists(username)).thenReturn(true)
        whenever(cachePort.find(username)).thenReturn(refreshToken)
        whenever(tokenProvider.getPrincipal(refreshToken)).thenReturn(principalResult)
        whenever(tokenProvider.generateToken(username, "USER")).thenReturn(tokenResult)

        val result = service.refresh(username)
        assertEquals(tokenResult.accessToken, result.accessToken)
        verify(cachePort).save(username, tokenResult.refreshToken, tokenResult.refreshTokenValidTime)
    }

    @Test
    fun `refresh token throws RefreshTokenNotFoundException`() {
        whenever(cachePort.exists("user@test.com")).thenReturn(false)
        assertFailsWith<RefreshTokenNotFoundException> {
            service.refresh("user@test.com")
        }
    }
}