package io.brieflyz.subscription_service.model.entity

import io.brieflyz.subscription_service.common.constants.SubscriptionInterval
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@SQLDelete(sql = "update subscription set deleted = true where id = ?")
@SQLRestriction("deleted is false")
class Subscription(
    val memberId: Long,
    val email: String,
    subscriptionInterval: SubscriptionInterval
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Enumerated(EnumType.STRING)
    var subscriptionInterval = subscriptionInterval
        protected set

    var deleted: Boolean = false
        protected set

    fun updateSubscriptionInterval(interval: SubscriptionInterval) {
        subscriptionInterval = interval
    }

    fun delete() {
        require(!deleted)
        deleted = true
    }
}