package io.brieflyz.subscription_service.infra.db

import io.brieflyz.subscription_service.model.entity.Subscription
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionRepository : JpaRepository<Subscription, Long>, SubscriptionQueryRepository {
    fun findByMemberIdOrEmail(memberId: Long?, email: String?): List<Subscription>
    fun existsByMemberId(memberId: Long): Boolean
}