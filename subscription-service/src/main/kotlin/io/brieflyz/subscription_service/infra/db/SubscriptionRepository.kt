package io.brieflyz.subscription_service.infra.db

import io.brieflyz.subscription_service.model.entity.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SubscriptionRepository : JpaRepository<Subscription, Long>, SubscriptionQueryRepository {
    fun findByMemberId(memberId: Long): Subscription?
    fun findByMemberIdOrEmail(memberId: Long?, email: String?): List<Subscription>

    @Query("select s from Subscription s where s.plan != 'UNLIMITED' and s.deleted = false")
    fun findLimitedSubscriptionsQuery(): List<Subscription>

    @Modifying(clearAutomatically = true)
    @Query("update Subscription s set s.deleted = true where s.id in :ids")
    fun softDeleteSubscriptionsInIdsQuery(ids: List<Long>)
}