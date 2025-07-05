package io.brieflyz.auth_service.controller

import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.model.dto.SignInRequestDTO
import io.brieflyz.auth_service.model.dto.SignUpRequestDTO
import io.brieflyz.auth_service.model.dto.TokenResponseDTO
import io.brieflyz.auth_service.service.AuthService
import io.brieflyz.core.constants.SuccessCode
import io.brieflyz.core.dto.api.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody body: SignUpRequestDTO): ApiResponse<Long> {
        val memberId = authService.join(body)
        return ApiResponse.success(SuccessCode.SIGN_UP_SUCCESS, memberId)
    }

    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.CREATED)
    fun signInByLocal(
        @Valid @RequestBody body: SignInRequestDTO,
        response: HttpServletResponse
    ): ApiResponse<TokenResponseDTO> {
        val token = authService.login(body)
        CookieUtils.addCookie(
            response,
            name = CookieName.ACCESS_TOKEN_COOKIE_NAME,
            value = CookieUtils.serialize(token.accessToken),
            maxAge = token.accessTokenValidTime
        )
        return ApiResponse.success(SuccessCode.SIGN_IN_SUCCESS, token)
    }
}