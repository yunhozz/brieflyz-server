package io.brieflyz.subscription_service.service

import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.common.exception.MailSendingException
import jakarta.mail.MessagingException
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter

@Service
class MailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {
    private val log = logger()

    @Async
    fun sendAsync(email: String, plan: String) {
        val message = mailSender.createMimeMessage()
        val now = LocalDateTime.now()

        try {
            MimeMessageHelper(message, true, "UTF-8").apply {
                setTo(email)
                setSubject("[Brieflyz] 구독 만료 안내 메일입니다.")

                val context = Context()
                context.setVariable("email", email)
                context.setVariable("planName", plan)
                context.setVariable("sentAt", now.format(FORMATTER_1))
                context.setVariable("expiryDate", now.format(FORMATTER_2))
                context.setVariable("renewUrl", "") // 구독 갱신 URL
                context.setVariable("supportUrl", "") // 고객 지원 URL
                context.setVariable("unsubscribeUrl", "") // 구독 취소 URL
                context.setVariable("year", Year.now().toString())

                setText(templateEngine.process("email", context), true)
            }

            mailSender.send(message)
            log.info("Success to send data on Email: id=${message.messageID}, sent=${message.sentDate}")

        } catch (e: Exception) {
            log.error(e.localizedMessage, e)
            when (e) {
                is MailException, is MessagingException -> throw MailSendingException(e.localizedMessage)
                else -> throw IllegalArgumentException(e.localizedMessage, e)
            }
        }
    }

    companion object {
        val FORMATTER_1: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val FORMATTER_2: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}