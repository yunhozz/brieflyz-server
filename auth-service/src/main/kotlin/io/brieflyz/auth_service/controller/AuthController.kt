package io.brieflyz.auth_service.controller

import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.common.utils.SerializationUtils
import io.brieflyz.auth_service.model.dto.request.SignInRequest
import io.brieflyz.auth_service.model.dto.request.SignUpRequest
import io.brieflyz.auth_service.model.dto.response.TokenResponse
import io.brieflyz.auth_service.service.AuthService
import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
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
    private val authService: AuthService
) {
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<Long> {
        val memberId = authService.join(request)
        return ApiResponse.success(SuccessStatus.SIGN_UP_SUCCESS, memberId)
    }

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    fun verifyEmail(@RequestParam token: String): ApiResponse<Void> {
        authService.verifyEmail(token)
        return ApiResponse.success(SuccessStatus.USER_SIGNUP_VERIFY_SUCCESS)
    }

    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.CREATED)
    fun signInByLocal(
        @Valid @RequestBody request: SignInRequest,
        response: HttpServletResponse
    ): ApiResponse<TokenResponse> {
        val token = authService.login(request)
        CookieUtils.addCookie(
            response,
            name = CookieName.ACCESS_TOKEN_COOKIE_NAME,
            value = SerializationUtils.serialize(token.accessToken),
            maxAge = token.accessTokenValidTime
        )
        return ApiResponse.success(SuccessStatus.SIGN_IN_SUCCESS, token)
    }
}