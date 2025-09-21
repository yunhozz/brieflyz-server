package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.port.out.CachePort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DeleteRefreshTokenServiceTest {

    private lateinit var cachePort: CachePort
    private lateinit var service: DeleteRefreshTokenService

    @BeforeEach
    fun setUp() {
        cachePort = mock()
        service = DeleteRefreshTokenService(cachePort)
    }

    @Test
    fun `delete refresh token successfully`() {
        val username = "user@test.com"
        service.delete(username)
        verify(cachePort).delete(username)
    }
}