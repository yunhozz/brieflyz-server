package io.brieflyz.subscription_service.adapter.out.persistence

import io.brieflyz.subscription_service.adapter.out.persistence.entity.PaymentEntity
import io.brieflyz.subscription_service.adapter.out.persistence.repository.PaymentRepository
import io.brieflyz.subscription_service.application.port.out.PaymentRepositoryPort
import io.brieflyz.subscription_service.domain.model.Payment
import io.brieflyz.subscription_service.domain.model.Subscription
import org.springframework.stereotype.Component

@Component
class PaymentJpaRepositoryAdapter(
    private val paymentRepository: PaymentRepository
) : PaymentRepositoryPort {

    override fun save(payment: Payment): Payment {
        val paymentEntity = paymentRepository.save(payment.toEntity())
        return paymentEntity.toDomain()
    }

    override fun findAllBySubscription(subscription: Subscription): List<Payment> =
        paymentRepository.findAllBySubscription(subscription.toEntity()).map { it.toDomain() }

    override fun deleteAllBySubscriptionId(subscriptionId: Long) {
        val paymentEntities = paymentRepository.findAllBySubscriptionId(subscriptionId)
        paymentRepository.deleteAll(paymentEntities) // cascade remove 'PaymentDetails'
    }
}

private fun Payment.toEntity() = PaymentEntity(id, subscription.toEntity(), charge, method, details.toEntity())

private fun PaymentEntity.toDomain() = Payment.create(subscription.toDomain(), charge, method, details.toDomain()!!, id)