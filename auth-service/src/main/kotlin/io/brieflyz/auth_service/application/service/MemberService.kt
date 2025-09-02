package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.MemberResponseDto
import io.brieflyz.auth_service.application.dto.TokenResponseDto
import io.brieflyz.auth_service.application.exception.RefreshTokenNotFoundException
import io.brieflyz.auth_service.application.exception.UserNotFoundException
import io.brieflyz.auth_service.common.auth.JwtProvider
import io.brieflyz.auth_service.common.redis.RedisHandler
import io.brieflyz.auth_service.domain.entity.Member
import io.brieflyz.auth_service.domain.repository.MemberRepository
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
class MemberService(
    private val memberRepository: MemberRepository,
    private val jwtProvider: JwtProvider,
    private val redisHandler: RedisHandler
) {
    private val log = logger()

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

        val member = findMemberByEmail(message.email)
        member.updateBySubscription(message.isCreated)

        ack.acknowledge()
    }

    fun refreshToken(username: String): TokenResponseDto {
        if (!redisHandler.exists(username)) throw RefreshTokenNotFoundException() // re-login

        val refreshToken = redisHandler.find(username)
        val authentication = jwtProvider.getAuthentication(refreshToken)
        val tokens = jwtProvider.generateToken(authentication)

        redisHandler.save(username, tokens.refreshToken, tokens.refreshTokenValidTime)

        return TokenResponseDto(tokens.tokenType + tokens.accessToken, tokens.accessTokenValidTime)
    }

    fun deleteRefreshToken(username: String) {
        redisHandler.delete(username)
    }

    @Transactional
    fun withdraw(username: String) {
        val member = findMemberByEmail(username)
        deleteRefreshToken(member.email)
        memberRepository.delete(member)
    }

    @Transactional(readOnly = true)
    fun findAllMembers(): List<MemberResponseDto> = memberRepository.findAll()
        .map { member -> member.toDto() }

    @Transactional(readOnly = true)
    fun findMemberById(memberId: Long): MemberResponseDto {
        val member = memberRepository.findById(memberId)
            ?: throw UserNotFoundException("Member ID: $memberId")
        return member.toDto()
    }

    private fun findMemberByEmail(email: String) = memberRepository.findByEmail(email)
        ?: throw UserNotFoundException("Email: $email")

    private fun Member.toDto() = MemberResponseDto(
        id = this.id,
        email = this.email,
        nickname = this.email,
        loginType = this.loginType,
        roles = this.getRoles()
    )
}