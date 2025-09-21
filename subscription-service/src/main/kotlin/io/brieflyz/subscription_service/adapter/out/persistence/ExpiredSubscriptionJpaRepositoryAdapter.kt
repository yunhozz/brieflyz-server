package io.brieflyz.subscription_service.adapter.out.persistence

import io.brieflyz.subscription_service.adapter.out.persistence.entity.ExpiredSubscriptionEntity
import io.brieflyz.subscription_service.adapter.out.persistence.repository.ExpiredSubscriptionRepository
import io.brieflyz.subscription_service.application.port.out.ExpiredSubscriptionRepositoryPort
import io.brieflyz.subscription_service.domain.model.ExpiredSubscription
import org.springframework.stereotype.Component

@Component
class ExpiredSubscriptionJpaRepositoryAdapter(
    private val expiredSubscriptionRepository: ExpiredSubscriptionRepository
) : ExpiredSubscriptionRepositoryPort {

    override fun saveAll(expiredSubscriptions: List<ExpiredSubscription>) {
        val expiredSubscriptionEntities = expiredSubscriptions.map { it.toEntity() }
        expiredSubscriptionRepository.saveAll(expiredSubscriptionEntities)
    }

    override fun deleteAll() {
        expiredSubscriptionRepository.deleteAllInBatch()
    }
}

private fun ExpiredSubscription.toEntity() = ExpiredSubscriptionEntity(id, email, plan)

private fun ExpiredSubscriptionEntity.toDomain() = ExpiredSubscription(id, email, plan)