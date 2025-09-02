package io.brieflyz.auth_service.application.service

import io.brieflyz.core.utils.logger
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Component
class MailProducer(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {
    private val log = logger()

    @Async
    fun sendAsync(email: String, context: Context) {
        val message = mailSender.createMimeMessage()

        try {
            MimeMessageHelper(message, true, "UTF-8").apply {
                setTo(email)
                setSubject("[Brieflyz] 회원가입 인증 메일입니다.")
                setText(templateEngine.process("authentication-email", context), true)
            }

            mailSender.send(message)
            log.info("Success to send data on Email: id=${message.messageID}, sent=${message.sentDate}")

        } catch (e: Exception) {
            log.error("There was a problem with sending mail: ${e.message}", e)
        }
    }
}