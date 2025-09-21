package io.brieflyz.subscription_service.adapter.out.email

import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.application.port.out.EmailPort
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.concurrent.CompletableFuture

@Component
class EmailAdapter(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) : EmailPort {
    private val log = logger()

    @Async
    override fun send(
        email: String,
        subject: String,
        template: String,
        contextMap: Map<String, Any>
    ): CompletableFuture<Boolean> =
        CompletableFuture.supplyAsync {
            val message = mailSender.createMimeMessage()
            val context = Context().apply { setVariables(contextMap) }

            try {
                MimeMessageHelper(message, true, "UTF-8").apply {
                    setTo(email)
                    setSubject(subject)
                    setText(templateEngine.process(template, context), true)
                }

                mailSender.send(message)
                log.info("Success to send data on Email: id=${message.messageID}, sent=${message.sentDate}")
                true

            } catch (e: Exception) {
                log.error("There was a problem with sending mail: ${e.message}", e)
                false
            }
        }
}