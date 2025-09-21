package io.brieflyz.auth_service.adapter.`in`.web.controller

import io.brieflyz.auth_service.adapter.`in`.web.dto.mapper.toResponse
import io.brieflyz.auth_service.adapter.`in`.web.dto.response.MemberResponse
import io.brieflyz.auth_service.adapter.`in`.web.dto.response.TokenResponse
import io.brieflyz.auth_service.application.port.`in`.DeleteRefreshTokenUseCase
import io.brieflyz.auth_service.application.port.`in`.FindAllMembersUseCase
import io.brieflyz.auth_service.application.port.`in`.FindMemberUseCase
import io.brieflyz.auth_service.application.port.`in`.TokenRefreshUseCase
import io.brieflyz.auth_service.application.port.`in`.WithdrawUseCase
import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.common.utils.SerializationUtils
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
    private val findMemberUseCase: FindMemberUseCase,
    private val findAllMembersUseCase: FindAllMembersUseCase,
    private val tokenRefreshUseCase: TokenRefreshUseCase,
    private val deleteRefreshTokenUseCase: DeleteRefreshTokenUseCase,
    private val withDrawUseCase: WithdrawUseCase
) {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun lookupAllMembers(): ApiResponse<List<MemberResponse>> {
        val members = findAllMembersUseCase.findAllMembers()
        return ApiResponse.success(SuccessStatus.USER_INFORMATION_READ_SUCCESS, members.toResponse())
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun lookupMember(@PathVariable id: Long): ApiResponse<MemberResponse> {
        val member = findMemberUseCase.findMemberById(id)
        return ApiResponse.success(SuccessStatus.USER_INFORMATION_READ_SUCCESS, member.toResponse())
    }

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.CREATED)
    fun refreshToken(@JwtSubject username: String, response: HttpServletResponse): ApiResponse<TokenResponse> {
        val token = tokenRefreshUseCase.refresh(username)
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
        deleteRefreshTokenUseCase.delete(username)
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
        deleteRefreshTokenUseCase.delete(username)
        withDrawUseCase.withdraw(username)
        return ApiResponse.success(SuccessStatus.USER_WITHDRAW_SUCCESS)
    }
}