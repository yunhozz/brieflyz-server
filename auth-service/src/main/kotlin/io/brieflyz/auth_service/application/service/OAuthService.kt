package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.command.SocialLoginCommand
import io.brieflyz.auth_service.application.dto.result.SocialUserResult
import io.brieflyz.auth_service.application.dto.result.TokenResult
import io.brieflyz.auth_service.application.port.`in`.OAuthAuthenticateSuccessUseCase
import io.brieflyz.auth_service.application.port.`in`.SocialLoginUseCase
import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.application.port.out.TokenProviderPort
import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.domain.model.Member
import io.brieflyz.core.utils.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialLoginService(
    private val memberRepositoryPort: MemberRepositoryPort
) : SocialLoginUseCase {

    private val log = logger()

    @Transactional
    override fun loginOrRegisterSocialUser(command: SocialLoginCommand): SocialUserResult {
        val email = command.email
        val member = memberRepositoryPort.findMemberByEmail(email)?.apply {
            if (this.loginType == LoginType.LOCAL) {
                log.info("Update Local Member with Social Profile")
                updateBySocialLogin()
                memberRepositoryPort.save(this)
            }
        } ?: run {
            val provider = command.provider
            log.info("Login by OAuth2.0 provider=$provider")
            val member = Member.forSocial(email, provider + "_" + command.providerId)
            memberRepositoryPort.save(member)
        }

        return SocialUserResult(member.email, member.roles.split("|"), command.attributes)
    }
}

@Service
class OAuthAuthenticateSuccessService(
    private val tokenProviderPort: TokenProviderPort,
    private val cachePort: CachePort
) : OAuthAuthenticateSuccessUseCase {

    override fun handleSuccess(username: String, roles: List<String>): TokenResult {
        val tokenResult = tokenProviderPort.generateToken(username, roles.joinToString("|"))
        cachePort.save(username, tokenResult.refreshToken, tokenResult.refreshTokenValidTime)
        return tokenResult
    }
}