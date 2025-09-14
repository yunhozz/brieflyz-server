package io.brieflyz.ai_service.adapter.out.ai.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.ai_service.adapter.out.ai.AbstractAiStructureGeneratorAdapter
import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.utils.logger
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class OpenAiStructureGeneratorAdapter(
    private val openAiChatModel: OpenAiChatModel,
    objectMapper: ObjectMapper
) : AbstractAiStructureGeneratorAdapter(objectMapper) {

    private val log = logger()

    override fun getProvider() = AiProvider.OPEN_AI

    override fun generateContent(prompt: String): Flux<String> {
        val userMessage = UserMessage(prompt)
        val aiPrompt = Prompt(userMessage)

        return openAiChatModel.stream(aiPrompt)
            .flatMap { response ->
                val generation = response.result

                if (generation.metadata.finishReason == "STOP") {
                    val usage = response.metadata.usage
                    log.info(
                        "[Open AI Token Usage] " +
                                "Prompt tokens: ${usage.promptTokens}, " +
                                "Completion tokens: ${usage.completionTokens}, " +
                                "Total tokens: ${usage.totalTokens}"
                    )
                }

                Flux.just(generation.output.text ?: "")
            }
    }
}