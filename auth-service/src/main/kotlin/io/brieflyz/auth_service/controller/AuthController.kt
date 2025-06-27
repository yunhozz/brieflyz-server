package io.brieflyz.auth_service.controller

import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.model.dto.MemberResponseDTO
import io.brieflyz.auth_service.model.dto.SignInRequestDTO
import io.brieflyz.auth_service.model.dto.SignUpRequestDTO
import io.brieflyz.auth_service.model.dto.TokenResponseDTO
import io.brieflyz.auth_service.service.AuthService
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.core.dto.api.SuccessCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    fun signIn(
        @Valid @RequestBody body: SignInRequestDTO,
        response: HttpServletResponse
    ): ApiResponse<TokenResponseDTO> {
        val token = authService.login(body)
        CookieUtils.addCookie(
            response,
            CookieUtils.ACCESS_TOKEN_COOKIE_NAME,
            token.accessToken,
            token.accessTokenValidTime
        )
        return ApiResponse.success(SuccessCode.SIGN_IN_SUCCESS, token)
    }

    @GetMapping("/members")
    @ResponseStatus(HttpStatus.OK)
    fun lookupAllMembers(): ApiResponse<List<MemberResponseDTO>> {
        val members = authService.findAllMembers()
        return ApiResponse.success(SuccessCode.USER_INFORMATION_READ_SUCCESS, members)
    }

    @GetMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun lookupMember(@PathVariable id: Long): ApiResponse<MemberResponseDTO> {
        val member = authService.findMemberById(id)
        return ApiResponse.success(SuccessCode.USER_INFORMATION_READ_SUCCESS, member)
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    fun refreshToken(
        @AuthenticationPrincipal principal: String,
        response: HttpServletResponse
    ): ApiResponse<TokenResponseDTO> {
        val token = authService.refreshToken(principal)
        CookieUtils.addCookie(
            response,
            CookieUtils.ACCESS_TOKEN_COOKIE_NAME,
            token.accessToken,
            token.accessTokenValidTime
        )
        return ApiResponse.success(SuccessCode.TOKEN_REFRESH_SUCCESS, token)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.CREATED)
    fun signOut(
        @AuthenticationPrincipal principal: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Any> {
        authService.deleteRefreshToken(principal)
        CookieUtils.deleteCookie(request, response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME)
        return ApiResponse.success(SuccessCode.LOGOUT_SUCCESS)
    }

    @DeleteMapping("/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun withdraw(
        @AuthenticationPrincipal principal: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Any> {
        authService.withdraw(principal)
        CookieUtils.deleteAllCookies(request, response)
        return ApiResponse.success(SuccessCode.USER_WITHDRAW_SUCCESS)
    }
}