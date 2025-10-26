package io.brieflyz.subscription_service.adapter.out.persistence.repository

import io.brieflyz.subscription_service.adapter.out.persistence.entity.SubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SubscriptionRepository : JpaRepository<SubscriptionEntity, Long> {
    fun findByEmail(email: String): SubscriptionEntity?

    @Query("select s from SubscriptionEntity s where s.plan != 'UNLIMITED' and s.deleted = false")
    fun findLimitedSubscriptionsQuery(): List<SubscriptionEntity>

    @Modifying(clearAutomatically = true)
    @Query("update SubscriptionEntity s set s.deleted = true where s.id in :subscriptionIds")
    fun softDeleteInIdsQuery(subscriptionIds: List<Long>)
}