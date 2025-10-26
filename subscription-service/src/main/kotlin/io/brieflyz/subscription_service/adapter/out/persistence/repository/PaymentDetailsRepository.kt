package io.brieflyz.subscription_service.adapter.out.persistence.repository

import io.brieflyz.subscription_service.adapter.out.persistence.entity.PaymentDetailsEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentDetailsRepository : JpaRepository<PaymentDetailsEntity, Long>