package io.brieflyz.subscription_service.service

import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.infra.db.SubscriptionRepository
import io.brieflyz.subscription_service.model.dto.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.SubscriptionResponse
import io.brieflyz.subscription_service.model.dto.SubscriptionUpdateRequest
import io.brieflyz.subscription_service.model.entity.Subscription
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository
) {
    @Transactional
    fun createSubscription(request: SubscriptionCreateRequest): Long {
        val (memberId, email, country, city, plan) = request
        val subscription = Subscription(
            memberId,
            email,
            country,
            city,
            plan = SubscriptionPlan.of(plan)
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
    fun getSubscriptionsByMemberIdOrEmail(memberId: Long?, email: String?): List<SubscriptionResponse> =
        subscriptionRepository.findByMemberIdOrEmail(memberId, email)
            .map { it.toResponse() }

    @Transactional
    fun updateSubscription(id: Long, request: SubscriptionUpdateRequest): Long {
        val subscription = findSubscriptionById(id)
        subscription.updateSubscriptionPlan(SubscriptionPlan.of(request.plan))
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
        email = this.email,
        country = this.country,
        city = this.city,
        plan = this.plan.name,
        deleted = this.deleted,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )
}