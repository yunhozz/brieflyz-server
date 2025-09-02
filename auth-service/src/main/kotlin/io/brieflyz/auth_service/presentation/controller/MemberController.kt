package io.brieflyz.auth_service.presentation.controller

import io.brieflyz.auth_service.application.service.MemberService
import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.common.utils.SerializationUtils
import io.brieflyz.auth_service.presentation.dto.mapper.toResponse
import io.brieflyz.auth_service.presentation.dto.response.MemberResponse
import io.brieflyz.auth_service.presentation.dto.response.TokenResponse
import io.brieflyz.core.annotation.JwtSubject
import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun lookupAllMembers(): ApiResponse<List<MemberResponse>> {
        val members = memberService.findAllMembers()
        return ApiResponse.success(SuccessStatus.USER_INFORMATION_READ_SUCCESS, members.toResponse())
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun lookupMember(@PathVariable id: Long): ApiResponse<MemberResponse> {
        val member = memberService.findMemberById(id)
        return ApiResponse.success(SuccessStatus.USER_INFORMATION_READ_SUCCESS, member.toResponse())
    }

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.CREATED)
    fun refreshToken(@JwtSubject username: String, response: HttpServletResponse): ApiResponse<TokenResponse> {
        val token = memberService.refreshToken(username)
        CookieUtils.addCookie(
            response,
            name = CookieName.ACCESS_TOKEN_COOKIE_NAME,
            value = SerializationUtils.serialize(token.accessToken),
            maxAge = token.accessTokenValidTime
        )
        return ApiResponse.success(SuccessStatus.TOKEN_REFRESH_SUCCESS, token.toResponse())
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun signOut(
        @JwtSubject username: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Void> {
        CookieUtils.deleteCookie(request, response, CookieName.ACCESS_TOKEN_COOKIE_NAME)
        memberService.deleteRefreshToken(username)
        return ApiResponse.success(SuccessStatus.LOGOUT_SUCCESS)
    }

    @DeleteMapping("/withdraw")
    @ResponseStatus(HttpStatus.OK)
    fun withdraw(
        @JwtSubject username: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Void> {
        CookieUtils.deleteAllCookies(request, response)
        memberService.withdraw(username)
        return ApiResponse.success(SuccessStatus.USER_WITHDRAW_SUCCESS)
    }
}