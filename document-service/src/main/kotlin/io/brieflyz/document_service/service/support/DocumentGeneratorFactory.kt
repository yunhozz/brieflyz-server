package io.brieflyz.document_service.service.support

import io.brieflyz.core.constants.DocumentType
import org.springframework.stereotype.Component

@Component
class DocumentGeneratorFactory(generators: Set<DocumentGenerator>) {

    private val generatorMap: Map<DocumentType, DocumentGenerator> = generators.associateBy { it.getDocumentType() }

    fun createByDocumentType(documentType: DocumentType): DocumentGenerator = generatorMap[documentType]
        ?: throw IllegalArgumentException("해당 문서 생성 서비스가 존재하지 않습니다.")
}