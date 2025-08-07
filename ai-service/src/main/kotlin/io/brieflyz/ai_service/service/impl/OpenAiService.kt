package io.brieflyz.ai_service.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.ai_service.common.enums.AiProvider
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class OpenAiService(
    private val openAiChatModel: OpenAiChatModel,
    objectMapper: ObjectMapper
) : AbstractAiService(objectMapper) {

    override fun getProvider() = AiProvider.OPEN_AI

    override fun generateContent(prompt: String): Flux<String> {
        val userMessage = UserMessage(prompt)
        val aiPrompt = Prompt(userMessage)

        return openAiChatModel.stream(aiPrompt)
            .flatMap { response ->
                val output = response.result.output
                log.debug(output.toString())
                Flux.just(output.text!!)
            }
    }
}