package io.brieflyz.ai_service.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.core.utils.logger
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class OllamaAiService(
    private val ollamaChatModel: OllamaChatModel,
    objectMapper: ObjectMapper
) : AbstractAiService(objectMapper) {

    private val log = logger()

    override fun getProvider() = AiProvider.OLLAMA

    override fun generateContent(prompt: String): Flux<String> {
        val userMessage = UserMessage(prompt)
        val aiPrompt = Prompt(userMessage)

        return ollamaChatModel.stream(aiPrompt)
            .flatMap { response ->
                log.debug(response.toString())

                val generation = response.result

                if (generation.metadata.finishReason == "stop") {
                    val usage = response.metadata.usage
                    log.info(
                        "[Ollama AI Token Usage] " +
                                "Prompt tokens: ${usage.promptTokens}, " +
                                "Completion tokens: ${usage.completionTokens}, " +
                                "Total tokens: ${usage.totalTokens}"
                    )
                }

                Flux.just(generation.output.text ?: "")
            }
    }
}