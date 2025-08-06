package io.brieflyz.ai_service.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.core.utils.logger
import reactor.core.publisher.Mono

abstract class AbstractAiService(
    private val objectMapper: ObjectMapper
) : AiService {

    protected val log = logger()

    companion object {
        const val DOCUMENT_FORMAT = "{ \"section1\": \"content1\", \"section2\": \"content2\", ... }"
        const val EXCEL_FORMAT =
            "{ \"Sheet1\": [[\"Column1\", \"Column2\"], [\"Data1\", \"Data2\"]], \"Sheet2\": [[...], [...]] }"
        const val POWERPOINT_FORMAT =
            "[{\"title\": \"슬라이드1 제목\", \"content\": \"슬라이드1 내용\", \"notes\": \"슬라이드1 메모\"}, ...]"
    }

    override fun generateStructuredContent(prompt: String, outputFormat: String): Mono<Map<String, Any>> {
        val structuredPrompt = buildString {
            appendLine(prompt).appendLine("\n")
            append("반환 형식은 다음과 같아야 합니다: ").append(outputFormat).appendLine()
            append("유효한 JSON 형식으로 반환해주세요.")
        }

        log.debug("Generating structured content with prompt: $structuredPrompt")

        return generateContent(structuredPrompt)
            .collectList()
            .map { it.joinToString("") }
            .map { content ->
                objectMapper.readValue(content, object : TypeReference<Map<String, Any>>() {})
            }
    }

    override fun generateDocumentStructure(title: String, sections: List<String>): Mono<Map<String, String>> {
        val prompt = buildString {
            append("제목: ").append(title).appendLine("\n")
            append("다음 섹션으로 구성된 문서의 내용을 생성해주세요:").appendLine()

            for (section in sections) {
                append("- ").append(section).appendLine()
            }
            appendLine("\n")
            append("각 섹션의 내용을 JSON 형식으로 반환해주세요. 각 섹션은 키가 되며, 값은 해당 섹션의 내용입니다.")
        }

        log.debug("Generating prompt about document structure: $prompt")

        return generateStructuredContent(prompt, DOCUMENT_FORMAT)
            .map { obj -> obj.mapValues { entry -> entry.value.toString() } }
    }

    override fun generateExcelStructure(title: String, content: String): Mono<Map<String, List<List<String>>>> {
        val prompt = buildString {
            appendLine("제목: $title")
            appendLine("내용: $content").appendLine()
            appendLine("위 정보를 기반으로 엑셀 파일의 구조를 생성해주세요.")
            appendLine("여러 시트로 구성될 수 있으며, 각 시트에는 행과 열로 구성된 데이터가 포함됩니다.")
            appendLine("첫 번째 행은 열 제목이어야 합니다.")
            append("JSON 형식으로 반환해주세요. 각 시트는 키가 되며, 값은 2차원 배열로 각 행의 데이터입니다.")
        }

        return generateStructuredContent(prompt, EXCEL_FORMAT)
            .map { obj ->
                obj.mapNotNull { (sheetName, sheetData) ->
                    val rows = (sheetData as? List<*>)?.mapNotNull { row ->
                        (row as? List<*>)?.map { it.toString() }
                    } ?: return@mapNotNull null
                    sheetName to rows
                }.toMap()
            }
    }

    override fun generatePptStructure(title: String, content: String): Mono<List<Map<String, String>>> {
        val prompt = buildString {
            appendLine("제목: $title")
            appendLine("내용: $content").appendLine()
            appendLine("위 정보를 기반으로 PPT 프레젠테이션의 슬라이드 구조를 생성해주세요.")
            appendLine("각 슬라이드에는 제목, 내용, 그리고 선택적으로 메모가 포함될 수 있습니다.")
            appendLine("슬라이드 목록을 JSON 형식으로 반환해주세요.")
            append("각 슬라이드는 객체여야 하며, 슬라이드 제목, 내용, 메모를 포함합니다.")
        }

        return generateStructuredContent(prompt, POWERPOINT_FORMAT)
            .map { obj ->
                if (obj.containsKey("slides") && obj["slides"] is List<*>) {
                    val rawSlides = obj["slides"] as List<*>
                    rawSlides.mapNotNull { slide ->
                        (slide as? Map<*, *>)
                            ?.mapKeys { it.key.toString() }
                            ?.mapValues { it.value?.toString() ?: "" }
                    }
                } else {
                    obj.values.mapNotNull { value ->
                        (value as? Map<*, *>)
                            ?.mapKeys { it.key.toString() }
                            ?.mapValues { it.value?.toString() ?: "" }
                    }
                }
            }
    }
}