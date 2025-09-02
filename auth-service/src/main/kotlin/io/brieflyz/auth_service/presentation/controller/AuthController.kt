package io.brieflyz.auth_service.presentation.controller

import io.brieflyz.auth_service.application.service.AuthApplicationService
import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.common.utils.SerializationUtils
import io.brieflyz.auth_service.presentation.dto.mapper.toDto
import io.brieflyz.auth_service.presentation.dto.mapper.toResponse
import io.brieflyz.auth_service.presentation.dto.request.SignInRequest
import io.brieflyz.auth_service.presentation.dto.request.SignUpRequest
import io.brieflyz.auth_service.presentation.dto.response.TokenResponse
import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authApplicationService: AuthApplicationService
) {
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<Long> {
        val memberId = authApplicationService.join(request.toDto())
        return ApiResponse.success(SuccessStatus.SIGN_UP_SUCCESS, memberId)
    }

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    fun verifyEmail(
        @RequestParam token: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Void> {
        authApplicationService.verifyEmail(token)
        CookieUtils.deleteCookie(request, response, CookieName.ACCESS_TOKEN_COOKIE_NAME)
        return ApiResponse.success(SuccessStatus.USER_SIGNUP_VERIFY_SUCCESS)
    }

    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.CREATED)
    fun signInByLocal(
        @Valid @RequestBody request: SignInRequest,
        response: HttpServletResponse
    ): ApiResponse<TokenResponse> {
        val token = authApplicationService.login(request.toDto())
        CookieUtils.addCookie(
            response,
            name = CookieName.ACCESS_TOKEN_COOKIE_NAME,
            value = SerializationUtils.serialize(token.accessToken),
            maxAge = token.accessTokenValidTime
        )

        return ApiResponse.success(SuccessStatus.SIGN_IN_SUCCESS, token.toResponse())
    }
}