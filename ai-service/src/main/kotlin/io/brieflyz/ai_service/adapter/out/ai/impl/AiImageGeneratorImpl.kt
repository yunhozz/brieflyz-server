package io.brieflyz.ai_service.adapter.out.ai.impl

import io.brieflyz.ai_service.adapter.out.ai.AiImageGenerator
import io.brieflyz.core.utils.logger
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class AiImageGeneratorImpl(
    private val imageAiWebClient: WebClient
) : AiImageGenerator {

    private val log = logger()

    override fun generateImageUrl(prompt: String): Mono<String> =
        imageAiWebClient.post()
            .bodyValue(GenerateImageRequest(prompt))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<GenerateImageResponse>() {})
            .doOnSuccess { log.info("Request success for generating image. Status=${it.data.status}") }
            .map { it.data }
            .flatMap { data ->
                Flux.interval(Duration.ZERO, Duration.ofSeconds(2))
                    .flatMap {
                        imageAiWebClient.get()
                            .uri("/{taskId}", data.task_id)
                            .retrieve()
                            .bodyToMono(object : ParameterizedTypeReference<GenerateImageResponse>() {})
                            .map { it.data }
                    }
                    .filter { it.status == "COMPLETED" || it.status == "FAILED" }
                    .next()
                    .flatMap { data ->
                        if (data.status == "COMPLETED") Mono.just(data.generated.firstOrNull().orEmpty())
                        else Mono.error(RuntimeException("Image generation failed for ${data.task_id}"))
                    }
            }

    data class GenerateImageRequest(
        val prompt: String,
        val webhook_url: String? = null,
        val structure_reference: String? = null,
        val structure_strength: Int? = null,
        val style_reference: String? = null,
        val adherence: Int? = null,
        val hdr: Int? = null,
        val resolution: String = "1k",
        val aspect_ratio: String = "classic_4_3",
        val model: String? = null,
        val creative_detailing: Int? = null,
        val engine: String? = null,
        val fixed_generation: Boolean? = null,
        val filter_nsfw: Boolean? = null,
        val styling: Styling? = null
    ) {
        data class Styling(
            val styles: List<Style>? = null,
            val characters: List<Character>? = null,
            val colors: List<ColorWeight>? = null
        )

        data class Style(val name: String, val strength: Int)
        data class Character(val id: String, val strength: Int)
        data class ColorWeight(val color: String, val weight: Double)
    }

    data class GenerateImageResponse(
        val data: GenerateImageData
    ) {
        data class GenerateImageData(
            val generated: List<String>,
            val task_id: String,
            val status: String,
            val has_nsfw: List<Boolean>? = null
        )
    }
}