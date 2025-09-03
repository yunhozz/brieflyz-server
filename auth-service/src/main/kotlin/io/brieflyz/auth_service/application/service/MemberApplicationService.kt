package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.MemberResult
import io.brieflyz.auth_service.application.dto.TokenResult
import io.brieflyz.auth_service.application.exception.RefreshTokenNotFoundException
import io.brieflyz.auth_service.common.auth.JwtProvider
import io.brieflyz.auth_service.common.redis.RedisHandler
import io.brieflyz.auth_service.domain.entity.Member
import io.brieflyz.auth_service.domain.service.MemberService
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.KafkaMessage
import io.brieflyz.core.dto.kafka.SubscriptionMessage
import io.brieflyz.core.utils.logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberApplicationService(
    private val memberService: MemberService,
    private val jwtProvider: JwtProvider,
    private val redisHandler: RedisHandler
) {
    private val log = logger()

    fun refreshToken(username: String): TokenResult {
        if (!redisHandler.exists(username)) throw RefreshTokenNotFoundException() // re-login

        val refreshToken = redisHandler.find(username)
        val authentication = jwtProvider.getAuthentication(refreshToken)
        val tokens = jwtProvider.generateToken(authentication)

        redisHandler.save(username, tokens.refreshToken, tokens.refreshTokenValidTime)

        return TokenResult(tokens.tokenType + tokens.accessToken, tokens.accessTokenValidTime)
    }

    fun deleteRefreshToken(username: String) {
        redisHandler.delete(username)
    }

    @Transactional
    @KafkaListener(topics = [KafkaTopic.SUBSCRIPTION_TOPIC])
    fun updateBySubscriptionStatus(
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET, required = false) offset: Long?,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Payload message: KafkaMessage,
        ack: Acknowledgment
    ) {
        log.debug(
            "[Kafka Received] key={}, topic={}, partition={}, offset={}, timestamp={}, message={}",
            key,
            topic,
            partition,
            offset,
            timestamp,
            message
        )
        require(message is SubscriptionMessage)
        memberService.updateBySubscription(message.email, message.isCreated)
        ack.acknowledge()
    }

    @Transactional
    fun withdraw(username: String) {
        memberService.deleteMember(username)
        deleteRefreshToken(username)
    }

    @Transactional(readOnly = true)
    fun findAllMembers(): List<MemberResult> = memberService.findAllMembers()
        .map { member -> member.toResult() }

    @Transactional(readOnly = true)
    fun findMemberById(memberId: Long): MemberResult {
        val member = memberService.findMember(memberId)
        return member.toResult()
    }

    private fun Member.toResult() = MemberResult(
        id = this.id,
        email = this.email,
        nickname = this.email,
        loginType = this.loginType,
        roles = this.getRoles()
    )
}