package io.brieflyz.auth_service.controller

import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.model.dto.MemberResponseDTO
import io.brieflyz.auth_service.model.dto.SignInRequestDTO
import io.brieflyz.auth_service.model.dto.SignUpRequestDTO
import io.brieflyz.auth_service.model.dto.TokenResponseDTO
import io.brieflyz.auth_service.service.AuthService
import io.brieflyz.core.config.AuthServiceProperties
import io.brieflyz.core.constants.SuccessCode
import io.brieflyz.core.dto.api.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val authServiceProperties: AuthServiceProperties
) {
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody body: SignUpRequestDTO): ApiResponse<Long> {
        val memberId = authService.join(body)
        return ApiResponse.success(SuccessCode.SIGN_UP_SUCCESS, memberId)
    }

    @PostMapping("/sign-in/local")
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

    @GetMapping("/sign-in/social")
    fun signInByOauth2(@RequestParam provider: String, response: HttpServletResponse) {
        val oAuth2Url = "${authServiceProperties.oauth?.authorizationUri}/$provider"
        response.sendRedirect(oAuth2Url)
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
        @AuthenticationPrincipal userDetails: UserDetails,
        response: HttpServletResponse
    ): ApiResponse<TokenResponseDTO> {
        val token = authService.refreshToken(userDetails.username)
        CookieUtils.addCookie(
            response,
            name = CookieName.ACCESS_TOKEN_COOKIE_NAME,
            value = CookieUtils.serialize(token.accessToken),
            maxAge = token.accessTokenValidTime
        )
        return ApiResponse.success(SuccessCode.TOKEN_REFRESH_SUCCESS, token)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun signOut(
        @AuthenticationPrincipal userDetails: UserDetails,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Any> {
        CookieUtils.deleteCookie(request, response, CookieName.ACCESS_TOKEN_COOKIE_NAME)
        authService.deleteRefreshToken(userDetails.username)
        return ApiResponse.success(SuccessCode.LOGOUT_SUCCESS)
    }

    @DeleteMapping("/withdraw")
    @ResponseStatus(HttpStatus.OK)
    fun withdraw(
        @AuthenticationPrincipal userDetails: UserDetails,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Any> {
        CookieUtils.deleteAllCookies(request, response)
        authService.withdraw(userDetails.username)
        return ApiResponse.success(SuccessCode.USER_WITHDRAW_SUCCESS)
    }
}