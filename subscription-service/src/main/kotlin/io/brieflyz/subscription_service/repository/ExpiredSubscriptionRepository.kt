package io.brieflyz.subscription_service.repository

import io.brieflyz.subscription_service.model.entity.ExpiredSubscription
import org.springframework.data.jpa.repository.JpaRepository

interface ExpiredSubscriptionRepository : JpaRepository<ExpiredSubscription, Long>