package io.brieflyz.subscription_service.model.entity

import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.SQLRestriction

@Entity
@SQLRestriction("deleted is false")
class Subscription(
    val memberId: Long,
    val email: String,
    val country: String,
    val city: String,
    plan: SubscriptionPlan
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Enumerated(EnumType.STRING)
    var plan: SubscriptionPlan = plan
        protected set

    var deleted: Boolean = false
        protected set

    fun updateSubscriptionPlan(plan: SubscriptionPlan) {
        this.plan = plan
    }

    fun delete() {
        require(!deleted)
        deleted = true
    }
}