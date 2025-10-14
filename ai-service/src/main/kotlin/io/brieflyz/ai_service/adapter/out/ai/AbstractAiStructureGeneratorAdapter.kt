package io.brieflyz.ai_service.adapter.out.ai

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.ai_service.application.port.out.AiStructureGeneratorPort
import io.brieflyz.core.dto.document.ExcelStructure
import io.brieflyz.core.dto.document.PowerPointStructure
import io.brieflyz.core.dto.document.Row
import io.brieflyz.core.dto.document.Section
import io.brieflyz.core.dto.document.Slide
import io.brieflyz.core.dto.document.WordStructure
import io.brieflyz.core.utils.logger
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class AbstractAiStructureGeneratorAdapter(
    private val objectMapper: ObjectMapper
) : AiStructureGeneratorPort {

    @Autowired
    private lateinit var aiImageGenerator: AiImageGenerator

    private val log = logger()

    abstract fun generateContent(prompt: String): Flux<out Any> // Generate stream of content by AI

    override fun generateWordStructure(title: String, content: String): Mono<WordStructure> {
        val wordPrompt = buildWordRequestPrompt(title, content)

        fun parseSection(map: Map<*, *>?): Section? {
            if (map == null) return null

            val subSections = (map["subsections"] as? List<*>)?.mapNotNull { subItem ->
                parseSection(subItem as? Map<*, *>)
            } ?: emptyList()

            return Section(
                heading = map["heading"]?.toString() ?: "",
                content = map["content"]?.toString() ?: "",
                subSections = subSections
            )
        }

        return generateStructuredContent(wordPrompt, WORD_FORMAT).map { wordMap ->
            val sections = (wordMap["sections"] as? List<*>)?.mapNotNull { sectionItem ->
                parseSection(sectionItem as? Map<*, *>)
            } ?: emptyList()

            WordStructure(
                title = wordMap["title"]?.toString() ?: "",
                description = wordMap["description"]?.toString() ?: "",
                sections = sections,
                summary = wordMap["summary"]?.toString() ?: ""
            )
        }
    }

    override fun generateExcelStructure(title: String, content: String): Mono<ExcelStructure> {
        val excelPrompt = buildExcelRequestPrompt(title, content)

        return generateStructuredContent(excelPrompt, EXCEL_FORMAT).map { sheetMap ->
            val sheets = sheetMap.mapNotNull { (sheetName, sheetData) ->
                val rows = (sheetData as? List<*>)?.mapNotNull { row ->
                    val data = (row as List<*>).mapNotNull { it.toString() }
                    Row(data)
                } ?: return@mapNotNull null
                sheetName to rows
            }.toMap()

            ExcelStructure(sheets)
        }
    }

    override fun generatePptStructure(title: String, content: String): Mono<PowerPointStructure> {
        val pptPrompt = buildPowerPointRequestPrompt(title, content)

        return generateStructuredContent(pptPrompt, PPT_FORMAT)
            .flatMap { slideMap ->
                val slidesMono = slideMap.mapNotNull { (slideName, slideData) ->
                    val map = slideData as? Map<*, *> ?: return@mapNotNull null
                    val slideTitle = map["title"]?.toString() ?: ""
                    val slideContent = map["content"]?.toString() ?: ""
                    val slideNotes = map["notes"]?.toString()

                    fun createSlide(imageUrl: String? = null) =
                        mapOf(
                            slideName to Slide(
                                title = slideTitle,
                                content = slideContent,
                                notes = slideNotes,
                                imageUrl
                            )
                        )

                    if (map["imageUrl"] == null) {
                        Mono.just(createSlide())
                    } else {
                        aiImageGenerator.generateImageUrl(slideContent)
                            .map { imageUrl -> createSlide(imageUrl) }
                    }
                }

                Mono.zip(slidesMono) { results ->
                    val slides = results.map { it as Map<String, Slide> }
                    PowerPointStructure(slides)
                }
            }
    }

    private fun generateStructuredContent(prompt: String, outputFormat: String): Mono<Map<String, Any>> {
        val structurePrompt = buildJsonRequestPrompt(prompt, outputFormat)

        log.debug("Generating structured content with prompt :\n$structurePrompt")

        return generateContent(structurePrompt)
            .collectList()
            .map { words ->
                val content = words.joinToString("").trim()
                    .removeSurrounding("```json", "```")
                    .removeSurrounding("```", "```")
                    .replace(Regex(",\\s*([}\\]])"), "$1") // 마지막 쉼표 제거
                log.debug("Content created by AI:\n$content")
                objectMapper.readValue(content, object : TypeReference<Map<String, Any>>() {})
            }
    }

    companion object {
        const val WORD_FORMAT = """
        {
            "title": "문서 제목",
            "description": "문서의 간략한 개요 또는 서문",
            "sections": [
                {
                    "heading": "섹션 제목",
                    "content": "섹션 본문 내용 (여러 문단 가능)",
                    "subsections": [
                        {
                            "heading": "하위 섹션 제목",
                            "content": "하위 섹션 본문 내용"
                        }
                    ]
                },
                ...
            ],
            "summary": "문서의 요약 또는 결론"
        }
        """
        const val EXCEL_FORMAT = """
        {
            "Sheet1": [
                ["Column1", "Column2", ...],
                ["Data1", "Data2", ...]
            ],
            "Sheet2": [
                ["ColumnA", "ColumnB", ...],
                ["DataA", "DataB", ...]
            ],
            ...
        }
        """
        const val PPT_FORMAT = """
        {
            "Slide1": {
                "title": "슬라이드1 제목",
                "content": "슬라이드1 내용",
                "notes": "슬라이드1 메모 (Optional)",
                "imageUrl": "이미지 URL"
            },
            "Slide2": {
                "title": "슬라이드2 제목",
                "content": "슬라이드2 내용",
                "notes": "슬라이드2 메모 (Optional)"
            },
            ...
        }
        """

        fun buildWordRequestPrompt(title: String, content: String) = buildString {
            appendLine("제목: $title")
            appendLine("내용: $content")
            appendLine()
            appendLine("위 정보를 기반으로 워드 파일의 구조를 생성해주세요.")
            appendLine()
            appendLine("문서의 제목(title)과 개요(description)을 포함하고 여러 개의 주요 섹션(sections)으로 구성할 것.")
            appendLine("각 섹션에는 제목(heading)과 본문(content)이 있으며, 필요시 하위 섹션(subsections)을 포함할 수 있음.")
            appendLine("각 content는 주제에 대한 충분한 설명을 포함해야 함.")
            appendLine("문서 끝에는 전체 내용을 요약(summary) 필드에 작성할 것.")
            appendLine("JSON 형식으로 반환하고 모두 영어로 작성할 것.")
        }

        fun buildExcelRequestPrompt(title: String, content: String) = buildString {
            appendLine("제목: $title")
            appendLine("내용: $content")
            appendLine()
            appendLine("위 정보를 기반으로 엑셀 파일의 구조를 생성해주세요.")
            appendLine()
            appendLine("여러 시트로 구성될 수 있으며, 각 시트에는 행과 열로 구성된 데이터를 포함할 것.")
            appendLine("첫 번째 행은 열 제목이어야 함.")
            appendLine("각 시트는 키가 되며, 값은 2차원 배열로 각 행의 데이터일 것.")
            appendLine("JSON 형식으로 반환하고 모두 영어로 작성할 것.")
        }

        fun buildPowerPointRequestPrompt(title: String, content: String) = buildString {
            appendLine("제목: $title")
            appendLine("내용: $content")
            appendLine()
            appendLine("위 정보를 기반으로 PPT 프레젠테이션의 슬라이드 구조를 생성해주세요.")
            appendLine()
            appendLine("각 슬라이드에는 제목, 내용, 그리고 선택적으로 메모가 포함될 수 있음.")
            appendLine("첫 번째 슬라이드에만 표지 이미지를 삽입하고 나머지 슬라이드에는 이미지를 넣지 말 것.")
            appendLine("JSON 형식으로 반환하고 모두 영어로 작성할 것.")
        }

        fun buildJsonRequestPrompt(prompt: String, outputFormat: String) = buildString {
            appendLine(prompt)
            appendLine()
            appendLine("반환 형식은 정확한 JSON이어야 하며 다음과 같아야 합니다: $outputFormat")
            appendLine("반드시 중괄호로 시작하고 중괄호로 끝나는 유효한 JSON 객체만 출력하세요.")
            appendLine("다른 텍스트나 설명 없이 JSON 데이터만 출력하세요. 코드블럭(```json)은 포함하지 마세요.")
        }
    }
}