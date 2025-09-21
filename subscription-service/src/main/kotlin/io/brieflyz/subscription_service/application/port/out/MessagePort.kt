package io.brieflyz.subscription_service.application.port.out

interface MessagePort {
    fun sendSubscriptionMessage(message: Any)
}