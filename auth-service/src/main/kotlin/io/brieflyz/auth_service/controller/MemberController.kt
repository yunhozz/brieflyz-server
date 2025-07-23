package io.brieflyz.auth_service.controller

import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.model.dto.MemberResponseDTO
import io.brieflyz.auth_service.model.dto.TokenResponseDTO
import io.brieflyz.auth_service.service.MemberService
import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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
    fun lookupAllMembers(): ApiResponse<List<MemberResponseDTO>> {
        val members = memberService.findAllMembers()
        return ApiResponse.success(SuccessStatus.USER_INFORMATION_READ_SUCCESS, members)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun lookupMember(@PathVariable id: Long): ApiResponse<MemberResponseDTO> {
        val member = memberService.findMemberById(id)
        return ApiResponse.success(SuccessStatus.USER_INFORMATION_READ_SUCCESS, member)
    }

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.CREATED)
    fun refreshToken(
        @AuthenticationPrincipal userDetails: UserDetails,
        response: HttpServletResponse
    ): ApiResponse<TokenResponseDTO> {
        val token = memberService.refreshToken(userDetails.username)
        CookieUtils.addCookie(
            response,
            name = CookieName.ACCESS_TOKEN_COOKIE_NAME,
            value = CookieUtils.serialize(token.accessToken),
            maxAge = token.accessTokenValidTime
        )
        return ApiResponse.success(SuccessStatus.TOKEN_REFRESH_SUCCESS, token)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun signOut(
        @AuthenticationPrincipal userDetails: UserDetails,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Any> {
        CookieUtils.deleteCookie(request, response, CookieName.ACCESS_TOKEN_COOKIE_NAME)
        memberService.deleteRefreshToken(userDetails.username)
        return ApiResponse.success(SuccessStatus.LOGOUT_SUCCESS)
    }

    @DeleteMapping("/withdraw")
    @ResponseStatus(HttpStatus.OK)
    fun withdraw(
        @AuthenticationPrincipal userDetails: UserDetails,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Any> {
        CookieUtils.deleteAllCookies(request, response)
        memberService.withdraw(userDetails.username)
        return ApiResponse.success(SuccessStatus.USER_WITHDRAW_SUCCESS)
    }
}