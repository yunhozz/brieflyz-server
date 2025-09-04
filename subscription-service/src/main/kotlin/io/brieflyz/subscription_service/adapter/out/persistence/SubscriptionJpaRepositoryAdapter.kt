package io.brieflyz.subscription_service.adapter.out.persistence

import io.brieflyz.subscription_service.adapter.out.persistence.entity.SubscriptionEntity
import io.brieflyz.subscription_service.adapter.out.persistence.repository.SubscriptionRepository
import io.brieflyz.subscription_service.application.port.out.SubscriptionRepositoryPort
import io.brieflyz.subscription_service.domain.model.Subscription
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class SubscriptionJpaRepositoryAdapter(
    private val subscriptionRepository: SubscriptionRepository
) : SubscriptionRepositoryPort {

    override fun save(subscription: Subscription): Subscription {
        val subscriptionEntity = subscriptionRepository.save(subscription.toEntity())
        return subscriptionEntity.toDomain()
    }

    override fun findById(subscriptionId: Long): Subscription? =
        subscriptionRepository.findByIdOrNull(subscriptionId)?.toDomain()

    override fun findByEmail(email: String): Subscription? =
        subscriptionRepository.findByEmail(email)?.toDomain()

    override fun delete(subscription: Subscription) {
        subscriptionRepository.delete(subscription.toEntity())
    }

    override fun softDeleteInIdsQuery(subscriptionIds: List<Long>) {
        subscriptionRepository.softDeleteInIdsQuery(subscriptionIds)
    }
}

internal fun Subscription.toEntity() = SubscriptionEntity(id, email, country, city, plan, payCount, deleted)

internal fun SubscriptionEntity.toDomain() = Subscription.create(email, country, city, plan, id)