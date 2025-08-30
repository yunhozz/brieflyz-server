package io.brieflyz.subscription_service.service.support

import io.brieflyz.core.utils.logger
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.concurrent.CompletableFuture

@Service
class MailProducer(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {
    private val log = logger()

    @Async
    fun sendAsync(email: String, subject: String, template: String, context: Context): CompletableFuture<Boolean> =
        CompletableFuture.supplyAsync {
            val message = mailSender.createMimeMessage()

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