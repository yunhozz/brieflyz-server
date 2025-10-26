package io.brieflyz.core.dto.document

data class WordStructure(
    val title: String,
    val description: String,
    val sections: List<Section>,
    val summary: String
)

data class Section(
    val heading: String,
    val content: String,
    val subSections: List<Section> = emptyList()
)

data class ExcelStructure(
    val sheets: Map<String, List<Row>>
)

data class Row(
    val data: List<String>
)

data class PowerPointStructure(
    val slides: List<Map<String, Slide>>
)

data class Slide(
    val title: String,
    val content: String,
    val notes: String?,
    val imageUrl: String? = null
)