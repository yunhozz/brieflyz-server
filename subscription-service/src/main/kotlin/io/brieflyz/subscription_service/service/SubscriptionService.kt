package io.brieflyz.subscription_service.service

import io.brieflyz.subscription_service.common.constants.SubscriptionInterval
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.infra.db.SubscriptionRepository
import io.brieflyz.subscription_service.model.dto.CreateSubscriptionRequest
import io.brieflyz.subscription_service.model.dto.SubscriptionResponse
import io.brieflyz.subscription_service.model.dto.UpdateSubscriptionRequest
import io.brieflyz.subscription_service.model.entity.Subscription
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository
) {
    @Transactional
    fun createSubscription(request: CreateSubscriptionRequest): Long {
        val (memberId, memberEmail, subscriptionInterval) = request
        val subscription = Subscription(
            memberId,
            memberEmail,
            subscriptionInterval = SubscriptionInterval.of(subscriptionInterval)
        )
        val savedSubscription = subscriptionRepository.save(subscription)

        return savedSubscription.id
    }

    @Transactional(readOnly = true)
    fun getSubscription(id: Long): SubscriptionResponse {
        val subscription = findSubscriptionById(id)
        return subscription.toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllSubscriptions(): List<SubscriptionResponse> =
        subscriptionRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getSubscriptionsByMemberId(memberId: Long): List<SubscriptionResponse> =
        subscriptionRepository.findByMemberId(memberId).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getSubscriptionsByMemberEmail(memberEmail: String): List<SubscriptionResponse> =
        subscriptionRepository.findByMemberEmail(memberEmail).map { it.toResponse() }

    @Transactional
    fun updateSubscription(id: Long, request: UpdateSubscriptionRequest): Long {
        val subscription = findSubscriptionById(id)
        val subscriptionInterval = SubscriptionInterval.of(request.subscriptionInterval)

        subscription.updateSubscriptionInterval(subscriptionInterval)

        return subscription.id
    }

    @Transactional
    fun deleteSubscription(id: Long) {
        val subscription = findSubscriptionById(id)
        subscription.delete()
    }

    @Transactional
    fun hardDeleteSubscription(id: Long) {
        val subscription = findSubscriptionById(id)
        subscriptionRepository.delete(subscription)
    }

    @Transactional(readOnly = true)
    fun existsByMemberId(memberId: Long): Boolean = subscriptionRepository.existsByMemberId(memberId)

    private fun findSubscriptionById(id: Long) = subscriptionRepository.findByIdOrNull(id)
        ?: throw SubscriptionNotFoundException("Subscription ID: $id")

    private fun Subscription.toResponse() = SubscriptionResponse(
        id = this.id,
        memberId = this.memberId,
        memberEmail = this.memberEmail,
        subscriptionInterval = this.subscriptionInterval.name,
        deleted = this.deleted,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )
}